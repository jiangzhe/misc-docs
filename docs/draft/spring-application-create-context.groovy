SpringApplication.createApplicationContext() {
  registerAnnotationConfigProcessors(registry) {
    registerPostProcessor(ConfigurationClassPostProcessor.class)
    registerPostProcessor(AutowiredAnnotationBeanPostProcessor.class)
    registerPostProcessor(RequiredAnnotationBeanPostProcessor.class)
    registerPostProcessor(CommonAnnotationBeanPostProcessor.class)
  }
}