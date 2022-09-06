package com.powershop.mapper;

import com.powershop.pojo.SearchItem;

import java.util.List;

public interface SearchItemMapper {
    List<SearchItem> selectSearchItem();

    SearchItem getSearchItemByItemId(Long itemId);

}
