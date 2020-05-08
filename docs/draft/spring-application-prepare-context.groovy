SpringApplication.prepareContext(ctx) {
  postProcessApplicationContext(ctx)
  applyInitializers(ctx)
  SpringApplicationRunListener.contextPrepared(ctx)
  load(ctx, sources) {
    BeanDefinitionLoader.load() {
      AnnotatedBeanDefinitionReader.registerBean(annotatedClass) {
        ConditionEvaluator.shouldSkip(metadata)
        processCommonDefinitionAnnotations(abd)
        BeanDefinitionCustomizer.customize(abd)
        BeanDefinitionRegistry.registerBeanDefinition(bd) {
          DefaultListableBeanFactory.registerBeanDefinition(bn, bd)
        }
      }
    }
  }
  SpringApplicationRunListener.contextLoaded(ctx)
}