1. Spring为什么要实现自己的类读取器MetadataReader，直接使用Java的反射存在什么问题？

ConfigurationClassParser.retrieveBeanMethodMetadata()

2. AutoConfiguration究竟是通过什么机制定义Bean的？@ConditionalOnMissingBean实在什么时候判定的？



### context.refresh()

AbstractApplicationContext.refresh() {
  prepareRefresh()
  prepareBeanFactory(beanFactory)
  postProcessBeanFactory(beanFactory)
  invokeBeanFactoryPostProcessors(beanFactory) {
    ConfigurationClassPostProcessor.processConfigBeanDefinitions(registry) {
      checkConfigurationClassCandidate(beanDef)
      ConfigurationClassParser.parse(candidates) {
        ConditionEvaluator.shouldSkip(metadata, phase)
        processMemberClasses(configClass, sourceClass) {
          may_processConfigurationClass(candidate)
        }
        processPropertySource(sourceClass)
        processComponentScan(sourceClass) {
          ComponentScanAnnotationParser.parse(componentScan) {
            findCandidateComponents(basePackage) {
              new SimpleMetadataReader(resource)
              isCandidateComponent(metadataReader)
            }
            _postProcessBeanDefinition(candidate, beanName)
            _processCommonDefinitionAnnotations(abd)
            checkCandidate(beanName, candidate)
            _registerBeanDefinition(definitionHolder, registry)
          }
          _parse(bdCand)
        }
        processImports(configClass, sourceClass) {
          _processImportSelector(candidate) {
            processImports(importClassNames)
          }
          _processImportBeanDefinitionRegistrar(candidate) {
            addImportBeanDefinitionRegistrar(configClass, registrar)
          }
          _processConfigurationClass(candidate)
        }
        _processImportResource(sourceClass)
        _processBeanMethods(sourceClass)
        _processInterfaces(configClass, sourceClass)
        _processSuperClass(sourceClass)
      }
      processDeferredImportSelectors() {
        AutoConfigurationImportSelector.selectImports(annotationMetadata) {
          SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration.class, classLoader)
        }
        processImports(configClass, sourceClass, importClassName)
      }
    }
  }
  registerBeanPostProcessors(beanFactory)
  initMessageSource()
  initApplicationEventMulticaster()
  onRefresh()
  registerListeners()
  finishBeanFactoryInitialization(beanFactory)
  finishRefresh()
}


