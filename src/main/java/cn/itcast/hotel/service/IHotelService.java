package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IHotelService extends IService<Hotel> {
    /**
     * DSL 过滤
     * @param params
     * @return
     * @throws IOException
     */
    public PageResult searchAndFilter(RequestParams params) throws IOException;
    /**
     * 城市,星级,品牌,价格动态变化
     * @return
     */
    public Map<String, List<String>> filters(RequestParams params);

    /**
     * 搜索框自动补全接口
     * @param prefix
     * @return
     */
    List<String> getSuggestions(String prefix);
}
