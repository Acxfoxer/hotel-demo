package cn.itcast.hotel.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class HotelDoc {
    private Long id;
    //点名
    private String name;
    //地址
    private String address;
    //价格
    private Integer price;
    //分数
    private Integer score;
    //品牌
    private String brand;
    //城市
    private String city;
    //星级
    private String starName;
    //商圈
    private String business;
    //地理坐标
    private String location;
    //图片
    private String pic;
    // 排序时的 距离值
    private Object distance;
    //广告标识
    private Boolean isAD;
    //自动补全标识
    private List<String> suggestion;

    public HotelDoc(Hotel hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.price = hotel.getPrice();
        this.score = hotel.getScore();
        this.brand = hotel.getBrand();
        this.city = hotel.getCity();
        this.starName = hotel.getStarName();
        this.business = hotel.getBusiness();
        this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
        this.pic = hotel.getPic();
        // 组装suggestion
        if(this.business.contains("、")){
            // business有多个值，需要切割
            String[] arr = this.business.split("、");
            // 添加元素
            this.suggestion = new ArrayList<>();
            this.suggestion.add(this.brand);
            Collections.addAll(this.suggestion, arr);
        }else {
            this.suggestion = Arrays.asList(this.brand, this.business);
        }
    }
}
