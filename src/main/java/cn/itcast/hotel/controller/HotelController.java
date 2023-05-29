package cn.itcast.hotel.controller;

import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description 酒店控制器
 * @Date 2023-05-26-11-16
 * @Author qianzhikang
 */
@RestController
@RequestMapping("hotel")
public class HotelController {
    @Resource
    private IHotelService iHotelService;
    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams requestParams) throws IOException {
        return iHotelService.search(requestParams);
    }

    @PostMapping("/filters")
    public Map<String, List<String>> filters(@RequestBody RequestParams requestParams) throws IOException {
        return iHotelService.filters(requestParams);
    }
}
