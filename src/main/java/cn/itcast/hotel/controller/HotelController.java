package cn.itcast.hotel.controller;

import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/hotel")
public class HotelController {
    @Resource
    IHotelService hotelService;


    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams requestParams) throws IOException {
        return hotelService.searchAndFilter(requestParams);
    }

    @PostMapping("/filters")
    public Map<String, List<String>> filter(@RequestBody RequestParams requestParams) throws IOException {
        return hotelService.filters(requestParams);
    }

    @GetMapping("suggestion")
    public List<String> getSuggestions(@RequestParam("key") String prefix) {
        return hotelService.getSuggestions(prefix);
    }
}
