package com.example.library.demo;

import java.util.List;

public interface ElasticService {
   void updateArticle(String id, String title, String description) throws Exception;
   List<ElasticServiceImpl.Book> search(String searchString) throws Exception;

}
