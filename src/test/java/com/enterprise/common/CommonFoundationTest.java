package com.enterprise.common;

import com.enterprise.common.error.CommonErrorCode;
import com.enterprise.common.exception.BizException;
import com.enterprise.common.response.PageResult;
import com.enterprise.common.response.Result;
import com.enterprise.common.util.PasswordUtils;
import com.enterprise.common.util.SensitiveMaskUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommonFoundationTest {

    @Test
    void resultBuildsSuccessAndFailure() {
        Result<String> success = Result.success("ok");
        Result<Void> fail = Result.fail(CommonErrorCode.TOKEN_EMPTY);

        assertThat(success.getCode()).isZero();
        assertThat(success.getData()).isEqualTo("ok");
        assertThat(success.getTimestamp()).isPositive();
        assertThat(fail.getCode()).isEqualTo(10001);
        assertThat(fail.getData()).isNull();
    }

    @Test
    void pageResultCalculatesPagesAndKeepsEmptyRecords() {
        PageResult<String> page = new PageResult<>(List.of("a", "b"), 5, 1, 2);
        PageResult<String> empty = new PageResult<>(null, 0, 1, 10);

        assertThat(page.getPages()).isEqualTo(3);
        assertThat(page.getRecords()).containsExactly("a", "b");
        assertThat(empty.getRecords()).isEmpty();

        page.setRecords(null);
        page.setTotal(10);
        page.setPageNum(2);
        page.setPageSize(5);
        page.setPages(2);
        assertThat(page.getRecords()).isEmpty();
        assertThat(page.getTotal()).isEqualTo(10);
        assertThat(page.getPageNum()).isEqualTo(2);
        assertThat(page.getPageSize()).isEqualTo(5);
        assertThat(page.getPages()).isEqualTo(2);
    }

    @Test
    void passwordUtilsEncodesAndMatches() {
        String encoded = PasswordUtils.encode("secret123");

        assertThat(encoded).isNotEqualTo("secret123");
        assertThat(PasswordUtils.matches("secret123", encoded)).isTrue();
        assertThat(PasswordUtils.matches("bad", encoded)).isFalse();
        assertThat(PasswordUtils.matches(null, encoded)).isFalse();
    }

    @Test
    void sensitiveMaskUtilsMasksCoreValues() {
        assertThat(SensitiveMaskUtils.maskPhone("13800000000")).isEqualTo("138****0000");
        assertThat(SensitiveMaskUtils.maskPhone("12")).isEqualTo("***");
        assertThat(SensitiveMaskUtils.maskEmail("admin@example.com")).isEqualTo("a***n@example.com");
        assertThat(SensitiveMaskUtils.maskEmail("ab@example.com")).isEqualTo("a***@example.com");
        assertThat(SensitiveMaskUtils.maskEmail("bad")).isEqualTo("***");
        assertThat(SensitiveMaskUtils.maskPassword("abc")).isEqualTo("******");
        assertThat(SensitiveMaskUtils.maskToken("1234567890abcdef")).isEqualTo("123456***abcdef");
        assertThat(SensitiveMaskUtils.maskToken("short")).isEqualTo("***");
        assertThat(SensitiveMaskUtils.maskText(null)).isNull();
        assertThat(SensitiveMaskUtils.maskText("password=abc authorization: Bearer token")).contains("password=******");
    }

    @Test
    void resultSettersAreUsableForSerializationFrameworks() {
        Result<String> result = new Result<>();

        result.setCode(9);
        result.setMsg("msg");
        result.setData("data");
        result.setTimestamp(123L);

        assertThat(result.getCode()).isEqualTo(9);
        assertThat(result.getMsg()).isEqualTo("msg");
        assertThat(result.getData()).isEqualTo("data");
        assertThat(result.getTimestamp()).isEqualTo(123L);
        assertThat(Result.<Void>fail(8, "manual").getCode()).isEqualTo(8);
    }

    @Test
    void bizExceptionCarriesErrorCode() {
        BizException ex = new BizException(CommonErrorCode.DATA_NOT_FOUND);

        assertThat(ex.getCode()).isEqualTo(10005);
        assertThat(ex.getMessage()).isEqualTo("数据不存在");
    }
}
