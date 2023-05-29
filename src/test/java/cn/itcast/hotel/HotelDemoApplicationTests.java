package cn.itcast.hotel;


import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@SpringBootTest
class HotelDemoApplicationTests {
    @Resource
    private IHotelService iHotelService;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 批量插入文档
     *
     * @throws IOException
     */
    @Test
    void batchInsert() throws IOException {
        BulkRequest request = new BulkRequest();
        List<Hotel> list = iHotelService.list();
        for (Hotel hotel : list) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            request.add(new IndexRequest("hotel")
                    .id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc), XContentType.JSON));
        }
        restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
    }

    /**
     * 自动补全
     */
    @Test
    void suggestion() throws IOException {
        // 创建请求模版
        SearchRequest request = new SearchRequest("hotel");
        request.source().suggest(
                new SuggestBuilder().addSuggestion("suggestions",
                        // 字段名称
                        SuggestBuilders.completionSuggestion("suggestion")
                                // 前缀
                                .prefix("h")
                                // 跳过重复
                                .skipDuplicates(true)
                                .size(10)));
        SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 解析结果
        Suggest suggest = search.getSuggest();
        CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
        List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
        for (CompletionSuggestion.Entry.Option option : options) {
            String text = option.getText().toString();
            System.out.println(text);
        }

    }
}
