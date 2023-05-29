package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.BooleanQuery;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {


    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 查询酒店列表
     *
     * @param requestParams 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult search(RequestParams requestParams) {
        // 分页参数
        Integer page = requestParams.getPage();
        Integer size = requestParams.getSize();
        String key = requestParams.getKey();
        // 构建查询参数
        SearchRequest request = new SearchRequest("hotel");
        // 分页参数
        request.source().from((page - 1) * size).size(size);

        // 添加离我最近的排序条件
        String location = requestParams.getLocation();
        if (StringUtils.isNotBlank(location)) {
            request.source()
                    // 举例排序
                    .sort(SortBuilders.geoDistanceSort("location", new GeoPoint(location))
                            // 升序，离我最近
                            .order(SortOrder.ASC)
                            // 千米
                            .unit(DistanceUnit.KILOMETERS));
        }


        // 多个搜索条件用 boolQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 搜索框关键字检索条件
        if (StringUtils.isNotBlank(key)) {
            // must 表示必须含有
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        } else {
            // 没有参数则搜索全部
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        // 城市条件
        if (StringUtils.isNotBlank(requestParams.getCity())) {
            // 精确匹配使用 termQuery
            boolQuery.filter(QueryBuilders.termQuery("city", requestParams.getCity()));
        }
        // 品牌条件
        if (StringUtils.isNotBlank(requestParams.getBrand())) {
            boolQuery.filter(QueryBuilders.termQuery("brand", requestParams.getBrand()));
        }
        // 星级条件
        if (StringUtils.isNotBlank(requestParams.getStarName())) {
            boolQuery.filter(QueryBuilders.termQuery("starName", requestParams.getStarName()));
        }
        // 价格区间条件
        if (requestParams.getMinPrice() != null && requestParams.getMaxPrice() != null) {
            // 范围匹配使用 rangeQuery
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(requestParams.getMinPrice()).lte(requestParams.getMaxPrice()));
        }
        /** 实现竞价排名集合基础查询权重排序 */
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                // 原始查询
                boolQuery,
                // 过滤出文档中 isAd 为 true 表示是广告的记录
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                QueryBuilders.termQuery("isAd", true),
                                // 权重 *10
                                ScoreFunctionBuilders.weightFactorFunction(10)
                        )
                });


        request.source().query(functionScoreQueryBuilder);
        try {
            // 搜索
            SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            // 解析结果
            return resolveResult(search);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Map<String, List<String>> filters(RequestParams requestParams) {
        SearchRequest request = new SearchRequest("hotel");
        // 结合查询条件
        // 多个搜索条件用 boolQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        String key = requestParams.getKey();
        // 搜索框关键字检索条件
        if (StringUtils.isNotBlank(key)) {
            // must 表示必须含有
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        } else {
            // 没有参数则搜索全部
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        // 城市条件
        if (StringUtils.isNotBlank(requestParams.getCity())) {
            // 精确匹配使用 termQuery
            boolQuery.filter(QueryBuilders.termQuery("city", requestParams.getCity()));
        }
        // 品牌条件
        if (StringUtils.isNotBlank(requestParams.getBrand())) {
            boolQuery.filter(QueryBuilders.termQuery("brand", requestParams.getBrand()));
        }
        // 星级条件
        if (StringUtils.isNotBlank(requestParams.getStarName())) {
            boolQuery.filter(QueryBuilders.termQuery("starName", requestParams.getStarName()));
        }
        // 价格区间条件
        if (requestParams.getMinPrice() != null && requestParams.getMaxPrice() != null) {
            // 范围匹配使用 rangeQuery
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(requestParams.getMinPrice()).lte(requestParams.getMaxPrice()));
        }
        request.source().query(boolQuery);
        // 不要文档信息
        request.source().size(0);
        // 品牌聚合
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg")
                .field("brand")
                .size(100)
        );
        // 城市聚合
        request.source().aggregation(AggregationBuilders
                .terms("cityAgg")
                .field("city")
                .size(100)
        );
        // 星级聚合
        request.source().aggregation(AggregationBuilders
                .terms("starNameAgg")
                .field("starName")
                .size(100)
        );
        try {
            // 聚合搜索
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            // 解析结果
            Map<String, List<String>> map = new HashMap<>();
            Aggregations aggregations = response.getAggregations();
            List<String> brandList = getStrings(aggregations,"brandAgg");
            List<String> cityAggList = getStrings(aggregations,"cityAgg");
            List<String> starNameAggList = getStrings(aggregations,"starNameAgg");
            map.put("brand", brandList);
            map.put("city", cityAggList);
            map.put("starName", starNameAggList);
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private List<String> getStrings(Aggregations aggregations,String aggName) {
        Terms aggTerms = aggregations.get(aggName);
        List<? extends Terms.Bucket> buckets = aggTerms.getBuckets();
        List<String> list = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            String key = bucket.getKeyAsString();
            list.add(key);
        }
        return list;
    }

    /**
     * 单个搜索条件
     */
    private void singleQuerySearch(SearchRequest request, String key) {
        if (StringUtils.isNotBlank(key)) {
            // 构建关键字搜索
            request.source().query(QueryBuilders.matchQuery("all", key));
        } else {
            // 没有参数则搜索全部
            request.source().query(QueryBuilders.matchAllQuery());
        }
    }


    /**
     * 解析参数
     *
     * @param searchResponse 搜索返回值
     * @return 分页结果
     */
    private PageResult resolveResult(SearchResponse searchResponse) {
        // 获取命中结果
        SearchHits searchHits = searchResponse.getHits();
        // 获取总记录数
        long total = searchHits.getTotalHits().value;
        // 获取记录集合
        SearchHit[] hits = searchHits.getHits();
        // 转换为List
        List<HotelDoc> collect = Arrays.stream(hits).map(item -> {
            HotelDoc hotelDoc = JSON.parseObject(item.getSourceAsString(), HotelDoc.class);
            // 获取排序结果
            Object[] sortValues = item.getSortValues();

            // 获取距离排序结果中的距离值
            if (sortValues.length > 0) {
                Object sortValue = sortValues[0];
                hotelDoc.setDistance(sortValue);
            }

            return hotelDoc;
        }).collect(Collectors.toList());
        // 构建分页返回
        return new PageResult(total, collect);
    }
}
