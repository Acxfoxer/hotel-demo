package cn.itcast.hotel.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.context.properties.ConfigurationProperties;


public class ElasticSearchClientUtils {
    public ElasticsearchClient getClient(String hostName,Integer port){
        //创建请求客户端
        RestClient restClient = RestClient.builder(new HttpHost(hostName,port,
                        HttpHost.DEFAULT_SCHEME_NAME))
                //设置http    异步请求配置
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                    //设置用户账号密码
                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,new
                            UsernamePasswordCredentials("elastic","123456"));
                    httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    return httpAsyncClientBuilder;
                }).build();
        return null;
    }
}
