package com.powershop.service;

import com.github.pagehelper.PageHelper;
import com.powershop.mapper.SearchItemMapper;
import com.powershop.pojo.SearchItem;
import com.powershop.utils.JsonUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SearchItemServiceImpl implements SearchItemService {
    @Autowired
    private SearchItemMapper searchItemMapper;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${ES_INDEX_NAME}")
    private String ES_INDEX_NAME;

    @Value("${ES_TYPE_NAME}")
    private String ES_TYPE_NAME;
    @Override
    public void importAll() throws IOException {
        if(!isExistsIndex()){
            createIndex();
        }
        int pageNum = 1;
        while (true) {
            PageHelper.startPage(pageNum, 1000);
            List<SearchItem> searchItemList = searchItemMapper.selectSearchItem();
            if(searchItemList==null || searchItemList.size()==0){
                break;
            }
            //1、从mysql查询数据
            BulkRequest bulkRequest = new BulkRequest();
            for (SearchItem searchItem : searchItemList) {
                //2、把数据导入es
                bulkRequest.add(new IndexRequest(ES_INDEX_NAME, ES_TYPE_NAME).source(JsonUtils.objectToJson(searchItem), XContentType.JSON));
            }
            restHighLevelClient.bulk(bulkRequest);
            pageNum++;
        }
    }

    private void createIndex() throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(ES_INDEX_NAME);
        createIndexRequest.settings("{\n" +
                "    \"number_of_shards\": 2,\n" +
                "    \"number_of_replicas\": 1\n" +
                "  }",XContentType.JSON);
        createIndexRequest.mapping(ES_TYPE_NAME,"{\n" +
                "  \"_source\": {\n" +
                "    \"excludes\": [\n" +
                "      \"item_desc\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"properties\": {\n" +
                "    \"id\":{\n" +
                "      \"type\": \"text\",\n" +
                "       \"index\": false\n" +
                "    },\n" +
                "    \"item_title\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_sell_point\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_price\": {\n" +
                "      \"type\": \"float\"\n" +
                "    },\n" +
                "    \"item_image\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"index\": false\n" +
                "    },\n" +
                "    \"item_category_name\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_desc\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    }\n" +
                "  }\n" +
                "}",XContentType.JSON);
        restHighLevelClient.indices().create(createIndexRequest);
    }

    private boolean isExistsIndex(){
        try {
            OpenIndexRequest openIndexRequest = new OpenIndexRequest(ES_INDEX_NAME);
            restHighLevelClient.indices().open(openIndexRequest);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    public List<SearchItem> list(String q, Integer page, Integer size) {
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(ES_INDEX_NAME);
            searchRequest.types(ES_TYPE_NAME);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.multiMatchQuery(q,"item_title","item_category_name","item_desc","item_sell_point"));
            /**
             *  page  form  size
             *   1     0     20
             *   2     20    20
             *   3     40    20
             * from=(page-1)*size
             */
            searchSourceBuilder.from((page-1)*size);
            searchSourceBuilder.size(size);
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<font color='red'>");
            highlightBuilder.postTags("</font>");
            highlightBuilder.field("item_title");
            searchSourceBuilder.highlighter(highlightBuilder);
            searchRequest.source(searchSourceBuilder);
            SearchResponse response = restHighLevelClient.search(searchRequest);
            SearchHits hits = response.getHits();
            SearchHit[] hitsArr = hits.getHits();
            List<SearchItem> searchItemList = new ArrayList<>();
            for (SearchHit hit : hitsArr) {
                SearchItem searchItem = JsonUtils.jsonToPojo(hit.getSourceAsString(), SearchItem.class);
                Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
                if(highlightFieldMap!=null && highlightFieldMap.size()>0){
                    HighlightField highlightField = highlightFieldMap.get("item_title");
                    searchItem.setItem_title(highlightField.getFragments()[0].toString());
                }
                searchItemList.add(searchItem);
            }
            return searchItemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public void insertDoc(Long itemId) throws IOException {
        SearchItem searchItem = searchItemMapper.getSearchItemByItemId(itemId);
        IndexRequest indexRequest = new IndexRequest(ES_INDEX_NAME,ES_TYPE_NAME);
        indexRequest.source(JsonUtils.objectToJson(searchItem),XContentType.JSON);
        restHighLevelClient.index(indexRequest);


    }
}
