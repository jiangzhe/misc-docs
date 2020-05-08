SpringApplication.refreshContext(ctx) {
  AbstractApplicationContext.refresh() {
    prepareRefresh()
    prepareBeanFactory(beanFactory)
    postProcessBeanFactory(beanFactory)
    invokeBeanFactoryPostProcessors(beanFactory)
    registerBeanPostProcessors(beanFactory)
    initMessageSource()
    initApplicationEventMulticaster()
    onRefresh()
    registerListeners()
    finishBeanFactoryInitialization(beanFactory)
    finishRefresh()
  }
}