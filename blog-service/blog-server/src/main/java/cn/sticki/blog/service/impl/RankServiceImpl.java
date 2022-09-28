package cn.sticki.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.sticki.blog.mapper.BlogMapper;
import cn.sticki.blog.pojo.domain.Blog;
import cn.sticki.blog.pojo.vo.RankHotVO;
import cn.sticki.blog.service.RankService;
import cn.sticki.common.result.RestResult;
import cn.sticki.user.client.UserClient;
import cn.sticki.user.dto.UserDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import feign.template.QueryTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author durance
 */
@Service
public class RankServiceImpl implements RankService {

    public static final String KEY_PREFIX = "rank-hot:";

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    UserClient userClient;

    @Resource
    BlogMapper blogMapper;

    @Override
    public List<RankHotVO> getRankHotToday() {
        // 1 进行判断 redis 是否已经创建了 今天的redis 排行榜缓存的key
        // 1.1 获取 day key
        long dayKey = getDayKey();
        // 获取redis数据
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet().reverseRangeWithScores(KEY_PREFIX + dayKey, 0, -1);
        if(typedTuples==null || typedTuples.size() == 0){
            // 如果没有创建，说明今天暂无热榜相关的信息，返回空信息
            return null;
        }
        // 2 如果创建了，封装响应信息
        //拿到set集合迭代器
        Iterator<ZSetOperations.TypedTuple<String>> iterator = typedTuples.iterator();
        List<RankHotVO> result = new ArrayList<>();
        while(iterator.hasNext()){
            //拿到这项信息
            ZSetOperations.TypedTuple<String> tuple = iterator.next();
            // 拿到 blodid
            String blogid = tuple.getValue();
            // 2.1 查询并设置博客信息
            Blog blog = blogMapper.selectById(blogid);
            RankHotVO rankHotVO = BeanUtil.copyProperties(blog, RankHotVO.class);
            // 2.2 查询并设置用户信息
            RestResult<UserDTO> user = userClient.getByUserId(blog.getAuthorId());
            System.out.println(user);
            System.out.println(user.getData());
            rankHotVO.setAuthor(user.getData());
            // 2.3 设置热度信息
            rankHotVO.setHot(tuple.getScore());
            result.add(rankHotVO);
        }

        return result;
    }



    /**
     * 获取 day key
     */
    public long getDayKey() {
        return System.currentTimeMillis() / (1000 * 60 * 60 * 24);
    }


}
