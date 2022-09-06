package com.powershop.service;

import com.powershop.pojo.SearchItem;

import java.io.IOException;
import java.util.List;

public interface SearchItemService {
    void importAll() throws IOException;

    List<SearchItem> list(String q, Integer page, Integer size);

    void insertDoc(Long itemId) throws IOException;

}
