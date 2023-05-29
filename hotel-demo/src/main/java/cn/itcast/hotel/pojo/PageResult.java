package cn.itcast.hotel.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description 分页结果
 * @Date 2023-05-26-11-17
 * @Author qianzhikang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult {
    private Long total;
    private List<HotelDoc> hotels;
}
