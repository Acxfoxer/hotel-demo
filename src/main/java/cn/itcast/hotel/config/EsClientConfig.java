package cn.itcast.hotel.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsClientConfig {

    @Value("${spring.elasticsearch.uris}")
    private String hosts;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    /**
     * 解析配置的字符串，转为HttpHost对象数组, hosts example:   127.0.0.1:9200,127.0.0.1:9300
     *
     */
    private HttpHost[] toHttpHost() {
        if (StringUtils.isEmpty(hosts)) {
            throw new RuntimeException("invalid elasticsearch configuration");
        }

        String[] hostArray = hosts.split(",");
        HttpHost[] httpHosts = new HttpHost[hostArray.length];
        HttpHost httpHost;
        for (int i = 0; i < hostArray.length; i++) {
            String[] strings = hostArray[i].split(":");
            httpHost = new HttpHost(strings[0], Integer.parseInt(strings[1]), "http");
            httpHosts[i] = httpHost;
        }
        return httpHosts;
    }

    @Bean(value = "client")
    public ElasticsearchClient elasticsearchClient() {
        return new ElasticsearchClient(getTransport());
    }

    @Bean
    public ElasticsearchAsyncClient elasticsearchAsyncClient() {
        return new ElasticsearchAsyncClient(getTransport());
    }

    /**
     * ElasticsearchClient 账户密码,host,设置
     * @return 返回transport
     */
    public ElasticsearchTransport getTransport(){
        HttpHost httpHost = HttpHost.create(hosts);
        RestClient restClient = RestClient.builder(httpHost)
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                    //设置账号密码
                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    //es账号密码
                    credentialsProvider.setCredentials(AuthScope.ANY, new
                            UsernamePasswordCredentials(username, password));
                    httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    return httpAsyncClientBuilder;
                }).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return transport;
    }

}
