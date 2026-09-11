package kr.co.wikibook.logbatch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.function.PredicateFilteringItemProcessor;
import org.springframework.batch.infrastructure.item.validator.ValidatingItemProcessor;
import org.springframework.batch.infrastructure.item.validator.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;

class FilteringTest {
    @DisplayName("프레디케이트에 걸린 아이템을 걸러낸다")
    @Test
    void filterByPredicate() throws Exception {
        var processor = new PredicateFilteringItemProcessor<Integer>(num -> num < 10);
        assertThat(processor.process(9)).isNull();
        assertThat(processor.process(10)).isEqualTo(10);
    }

    @DisplayName("검증에 실패한 아이템을 예외 없이 걸러낸다")
    @Test
    void filterByValidate() {
        var processor = new ValidatingItemProcessor<Integer>(
                num -> {
                    if (num < 10) {
                        throw new ValidationException(num + " is less than 10");
                    }
                }
        );
        processor.setFilter(true);
        assertThat(processor.process(9)).isNull();
        assertThat(processor.process(10)).isEqualTo(10);
    }
}
