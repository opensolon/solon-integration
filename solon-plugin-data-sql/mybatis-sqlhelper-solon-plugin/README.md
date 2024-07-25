



```yaml
#分页组件的配置
sqlhelper:
    mybatis:
        instrumentor:
            dialect: "mysql"
            cache-instrumented-sql: true
            subquery-paging-start-flag: "[PAGING_StART]"
            subquery-paging-end-flag: "[PAGING_END]"
        pagination:
            count: true
            default-page-size: 10
            use-last-page-if-page-no-out: true
            count-suffix: _COUNT
```

