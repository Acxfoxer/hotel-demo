package cn.itcast.hotel.mq;

import cn.itcast.hotel.constant.MyConstants;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HotelListener {
    @Autowired
    IHotelService hotelService;
    /**
     * 监听酒店修改,新增的业务
     * @param id 酒店id
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = MyConstants.HOTEL_INSERT_QUEUE),
    exchange = @Exchange(value = MyConstants.HOTEL_EXCHANGE,type = ExchangeTypes.TOPIC),
    key = MyConstants.HOTEL_INSERT_KEY))
    public void listenHotelInsert(Long id){
        hotelService.insertById(id);
    }

    /**
     *
     * @param id 酒店Id
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = MyConstants.HOTEL_DELETE_QUEUE),
            exchange = @Exchange(value = MyConstants.HOTEL_EXCHANGE,type = ExchangeTypes.TOPIC),
            key = MyConstants.HOTEL_DELETE_KEY))
    public void listenHotelDelete(Long id){
        hotelService.deleteById(id);
    }
}
