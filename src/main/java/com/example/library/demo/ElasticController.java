package com.example.library.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ElasticController {

    private final ElasticServiceImpl elasticServiceImpl;

    @PutMapping("/books")
    public String addArticle(@RequestParam("title") String title,
                             @RequestParam("description") String description) throws Exception {
        String id = UUID.randomUUID().toString();
        elasticServiceImpl.updateArticle(id, title, description);
        return id;
    }

    @GetMapping("/search")
    public List<ElasticServiceImpl.Book> search(@RequestParam("query") String query) throws Exception {
        return elasticServiceImpl.search(query);
    }
}

