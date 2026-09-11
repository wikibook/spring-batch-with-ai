package kr.co.wikibook.batch.hello;

import kr.co.wikibook.batch.hello.job.Hello2JobConfig;
import kr.co.wikibook.batch.hello.job.HelloJobConfig;
import kr.co.wikibook.batch.hello.job.HelloParamJobConfig;
import kr.co.wikibook.batch.hello.job.SettleOrderJobConfig;
import kr.co.wikibook.batch.hello.job.SlowJobConfig;
import kr.co.wikibook.batch.hello.job.SpendTimeChunkJobConfig;
import org.springframework.context.annotation.Import;

@Import({
    HelloJobConfig.class,
    Hello2JobConfig.class,
    HelloParamJobConfig.class,
    SpendTimeChunkJobConfig.class,
    SettleOrderJobConfig.class,
    SlowJobConfig.class
}
)
public class HelloJobGroupContexts {
}
