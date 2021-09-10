package com.example.library.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ElasticServiceImpl implements ElasticService {

    private final static String INDEX_NAME = "books";
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestHighLevelClient elasticClient;
   // private final Book book;

    public static class Book {

        private String title;
        private String description;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
    public void updateArticle(String id, String title, String description) throws Exception {
        Book book = new Book();
        book.setTitle(title);
        book.setDescription(description);
        IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
        indexRequest.id(id);
        indexRequest.source(mapper.writeValueAsString(book), XContentType.JSON);
        elasticClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    public List<Book> search(String searchString) throws Exception {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(
                QueryBuilders.multiMatchQuery(searchString, "title", "description")
                        .fuzziness(Fuzziness.TWO)
                        .boost(1.0f)
                        .prefixLength(0)
                        .boost(1)
                        .fuzzyTranspositions(true));

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field(new HighlightBuilder.Field("title"))
                .field(new HighlightBuilder.Field("description"));
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
        List<Book> books = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String title = (String) sourceAsMap.get("title");
            String description = (String) sourceAsMap.get("description");

            HighlightField highlightFieldText = hit.getHighlightFields().get("description");
            if (highlightFieldText != null && highlightFieldText.fragments().length > 0) {
                description = highlightFieldText.fragments()[0].toString();
            }

            Book book = new Book();
            book.setTitle(title);
            book.setDescription(description);
            books.add(book);
        }

        return books;
    }
}
