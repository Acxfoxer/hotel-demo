package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggest;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class DslTest {
    @Autowired
    private ElasticsearchClient client;

    /**
     * 测试聚合
     */
    @Test
    public void testAggregation() throws IOException {
        //1.构建查询条件构造器
        RangeQuery.Builder range = QueryBuilders.range();
        //1.1设置查询条件
        RangeQuery rangeQuery = range.field("price")
                .lte(JsonData.of(20000))
                .gte(JsonData.of(1000))
                .build();
        //1.2设置精确聚合函数
        TermsAggregation.Builder terms = new TermsAggregation.Builder();
        NamedValue<SortOrder> count = NamedValue.of("_count", SortOrder.Desc);
        List<NamedValue<SortOrder>> list = new ArrayList<>();
        list.add(count);
        TermsAggregation termsAggregation = terms.order(list).size(25).field("brand").build();
        SearchResponse<HotelDoc> response = client.search(s -> s
                .index("hotel")
                .size(0)
                .query(rangeQuery._toQuery())
                .aggregations("brandAgg", a -> a
                        .terms(termsAggregation)), HotelDoc.class);
        Map<String, Aggregate> aggregations = response.aggregations();
        List<StringTermsBucket> buckets = aggregations.get("brandAgg").sterms().buckets().array();
        for (StringTermsBucket bucket : buckets) {
            System.out.println("There are " + bucket.docCount() +
                    " bikes under " + bucket.key());
        }
    }

    /**
     * 自动补全测试
     */
    @Test
    public void testCompletion() throws IOException {
        //创建suggestion构造器
        SearchRequest request = new SearchRequest.Builder()
                //索引
                .index("hotel")
                //suggest条件
                .suggest(s -> s
                        //查询结果为mySuggest
                        .suggesters("mySuggest", ss -> ss
                                //前缀
                                .prefix("h")
                                //completion条件
                                .completion(c -> c
                                        .field("suggestion")
                                        .skipDuplicates(true)
                                        .size(10)))).build();

        SearchResponse<HotelDoc> response = client.search(request, HotelDoc.class);
        Map<String, List<Suggestion<HotelDoc>>> suggest = response.suggest();
        List<Suggestion<HotelDoc>> mySuggest = suggest.get("mySuggest");
        for (Suggestion<HotelDoc> hotelDocSuggestion : mySuggest) {
            for (CompletionSuggestOption<HotelDoc> option : hotelDocSuggestion.completion().options()) {
                String text = option.text();
                System.out.println(text);
            }
        }
    }
}
