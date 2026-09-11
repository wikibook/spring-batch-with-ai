package kr.co.wikibook.logbatch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.support.CompositeItemReader;
import org.springframework.batch.infrastructure.item.support.IteratorItemReader;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeItemReaderTest {
    @DisplayName("여러 리더를 앞에서부터 이어서 읽는다")
    @Test
    void read() throws Exception {
        ItemStreamReader<String> reader1 = stringReader("a", "b");
        ItemStreamReader<String> reader2 = stringReader("c");
        var compositeReader = new CompositeItemReader<String>(List.of(reader1, reader2));
        compositeReader.open(new ExecutionContext());

        assertThat(compositeReader.read()).isEqualTo("a");
        assertThat(compositeReader.read()).isEqualTo("b");
        assertThat(compositeReader.read()).isEqualTo("c");
    }

    private ItemStreamReader<String> stringReader(String ... str) {
        Iterator<String> itr = List.of(str).iterator();
        return new IteratorItemReader<>(itr)::read;
    }
}
