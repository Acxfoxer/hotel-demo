package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootTest
@Slf4j
class HotelDemoApplicationTests {
    @Autowired
    private IHotelService hotelService;
    @Autowired
    private ElasticsearchClient client;

    /*
    设置es客户端
    @BeforeEach
    public void before() {
        RestClient restClient = RestClient.builder(HttpHost.create("http://192.168.136.101:9200"))
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                    //设置账号密码
                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    //es账号密码
                    credentialsProvider.setCredentials(AuthScope.ANY, new
                            UsernamePasswordCredentials("elastic", "123456"));
                    httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    return httpAsyncClientBuilder;
                }).build();
        RestClientTransport restClientTransport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        client = new ElasticsearchClient(restClientTransport);
    }*/

    /**
     * java api 索引操作
     */
    //创造索引库
    @Test
    public void CreateIndex() throws IOException {
        String indexName = "hope2";
        String aliases = "alias_hope2";
        Map<String, Property> propertyMap = new HashMap<>();
        propertyMap.put("name", new Property(new TextProperty.Builder().index(true).store(true).build()));
        propertyMap.put("age", new Property(new IntegerNumberProperty.Builder().index(false).build()));
        propertyMap.put("sex", new Property(new BooleanProperty.Builder().index(false).build()));
        TypeMapping typeMapping = new TypeMapping.Builder()
                .properties(propertyMap)
                .build();
        IndexSettings indexSettings = new IndexSettings
                .Builder()
                .numberOfShards(String.valueOf(1))
                .numberOfReplicas(String.valueOf(0))
                .build();
        CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                .index(indexName)
                .aliases(aliases, new Alias.Builder().isWriteIndex(true).build())
                .mappings(typeMapping)
                .settings(indexSettings)
                .build();
        CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest);
        System.out.println(createIndexResponse);
    }

    //获取索引的映射信息,别名,设置
    @Test
    public void getIndexWithMappingAndSettings() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest.Builder().index("hotel").build();
        GetIndexResponse getIndexResponse = client.indices().get(getIndexRequest);
        IndexState hotel = getIndexResponse.get("hotel");
        assert hotel.mappings() != null;
        System.out.println(JSON.toJSONString(hotel.mappings().properties()));
        System.out.println(JSON.toJSONString(hotel.aliases()));
        System.out.println(JSON.toJSONString(hotel.settings()));
    }

    //删除索引
    @Test
    public void deleteIndex() throws IOException {
        List<String> indexList = Collections.singletonList("hotel");
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest.Builder().index(indexList).build();
        DeleteIndexResponse deleteIndexResponse = client.indices().delete(deleteIndexRequest);
        System.out.println(deleteIndexResponse);
    }

    //根据索引值查看索引的值
    @Test
    public void getSDataByIndexAndIdTest() throws IOException {
        GetRequest getRequest = new GetRequest.Builder().index("hotel").id("id").build();
        GetResponse<Hotel> hotelGetResponse = client.get(getRequest, Hotel.class);
        Hotel hotel = hotelGetResponse.source();
        System.out.println(hotel);
    }

    //查看文档内容是否存在
    @Test
    public void existsDocument() throws IOException {
        GetRequest getRequest = new GetRequest.Builder().index("hotel").id("0").build();
        GetResponse<HotelDoc> hotelGetResponse = client.get(getRequest, HotelDoc.class);
        System.out.println(hotelGetResponse.source());
        System.out.println(hotelGetResponse.found());
    }

    //保持单条内容(对象)到文档
    @Test
    public void saveDocument() throws IOException {
        Hotel hotel = hotelService.getById(60223);
        HotelDoc hotelDoc = new HotelDoc(hotel);
        IndexRequest<HotelDoc> hotelIndexRequest = new IndexRequest.Builder<HotelDoc>().index("hotel").id(String.valueOf(60223))
                .document(hotelDoc)
                .build();
        IndexResponse indexResponse = client.index(hotelIndexRequest);

        System.out.println(indexResponse);
    }

    //批量导入文档
    @Test
    public void bulkDocumentList() throws IOException {
        List<Hotel> hotelList = hotelService.list();
        int i = 0;
        BulkRequest.Builder br = new BulkRequest.Builder();
        //循环添加
        for (Hotel hotel : hotelList) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            int finalI = i;
            br.operations(op ->
                    op.index(idx -> idx
                            .index("hotel")
                            .id(String.valueOf(finalI)).document(hotelDoc))
            );
            i++;
        }
        BulkResponse result = client.bulk(br.build());
        //如果日志报错
        if (result.errors()) {
            log.error("Bulk and errors");
            for (BulkResponseItem item : result.items()) {
                if (item.error() != null) {
                    log.error(item.error().reason());
                }
            }
        }
    }

    /**
     * java api 文档操作
     * 测试match_all()
     */
    @Test
    void testMatchAll() throws IOException {
        //函数编程,查询,是match_all查询
        MatchAllQuery query = new MatchAllQuery.Builder().build();
        SearchResponse<HotelDoc> response = client.search(s -> s
                        .index("hotel")
                        .query(q -> q.matchAll(query)
                        ),
                HotelDoc.class
        );
        TotalHits total = response.hits().total();
        System.out.println(total.value());
        //解析查询到的对象
        List<Hit<HotelDoc>> hits = response.hits().hits();
        for (Hit<HotelDoc> hit : hits) {
            HotelDoc hotelDoc = hit.source();
            System.out.println(hotelDoc);
        }
    }

    /**
     * 全文检索查询 match multi_match
     *
     * @throws IOException
     */
    @Test
    void testMatch() throws IOException {
        SearchResponse<HotelDoc> response = client.search(s -> s
                .index("hotel")
                .query(q -> q.match(t -> t.field("all").query("如家"))), HotelDoc.class);
        TotalHits total = response.hits().total();

        System.out.println(total);
    }

    /**
     * 测试多字段查询 mulit_match
     *
     * @return
     */
    @Test
    void testMulitMatch() throws IOException {
        SearchResponse<HotelDoc> response = client.search(s -> s
                .index("hotel")
                .query(q -> q.multiMatch(t -> t
                        .fields("brand", "name")
                        .query("如家"))), HotelDoc.class);
        System.out.println(response.hits().total().value());
    }

    /**
     * java api term 测试
     *
     * @throws IOException
     */

    @Test
    void testTerm() throws IOException {
        SearchResponse<HotelDoc> res = client.search(s -> s
                .index("hotel")
                .query(q -> q
                        .term(t -> t
                                .field("city")
                                .value("上海"))), HotelDoc.class);
        System.out.println("条数为:" + res.hits().total().value());
    }

    /**
     * JAVA API range 测试
     *
     * @throws IOException
     */
    @Test
    void testRange() throws IOException {
        SearchResponse<HotelDoc> res = client.search(s -> s
                .index("hotel")
                .query(q -> q
                        .range(r -> r.field("price")
                                .lte(JsonData.of(3000))
                                .gte(JsonData.of(1000)))), HotelDoc.class);
        System.out.println("条数为:" + res.hits().total().value());
    }

    /**
     * JAVA API bool 测试
     * @throws IOException
     */

    @Test
    void testBool() throws IOException {
        SearchResponse<HotelDoc> response = client.search(s -> s
                .index("hotel").query(q -> q.bool(b -> b.must(m -> m
                                .term(t -> t.field("city").value("杭州")))
                                .filter(f -> f.range(r -> r.field("price").lte(JsonData.of(250))))
                                .filter(f -> f.match(m -> m.field("all").query("级"))))), HotelDoc.class);
        System.out.println("条数为:" + response.hits().total().value());
        System.out.println();
    }

    /**
     * 排序测试
     * @throws IOException
     */
    @Test
    void testSort()throws IOException{
        SearchResponse<HotelDoc> response = client.search(s -> s.index("hotel")
                .sort(ss->ss
                        .field(f->f
                        .field("id")
                                .order(SortOrder.Asc))), HotelDoc.class);
        System.out.println("总条数为"+response.hits().total().value());
        List<Hit<HotelDoc>> hits = response.hits().hits();
        hits.forEach(hit-> System.out.println(hit.source()));
    }

    /**
     * 分页测试
     * @throws IOException
     */
    @Test
    void testPage()throws IOException{
        SearchResponse<HotelDoc> response = client.search(s -> s.index("hotel").
                sort(ss -> ss.field(f -> f.field("price").order(SortOrder.Asc)))
                .from(0)
                .size(10), HotelDoc.class);
        System.out.println("总条数为"+response.hits().total().value());
        List<Hit<HotelDoc>> hits = response.hits().hits();
        hits.forEach(hit-> System.out.println(hit.source()));
    }

    /**
     * 高亮测试
     * @throws IOException
     */
    @Test
    void testHighLight()throws IOException{
        SearchResponse<HotelDoc> response = client.search(s -> s.index("hotel").query(q -> q
                        .match(m -> m.field("all").query("如家")))
                .highlight(h -> h.fields("name", HighlightField.of(f -> f.requireFieldMatch(false)))), HotelDoc.class);
        System.out.println("高亮的总条数为:"+response.hits().total().value());
        response.hits().hits().forEach(hit-> System.out.println(hit.highlight()));
    }
}
