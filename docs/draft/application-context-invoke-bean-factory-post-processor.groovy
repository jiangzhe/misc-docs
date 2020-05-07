AbstractApplicationContext.invokeBeanFactoryPostProcessors(beanFactory) {
  _invokeBeanDefinitionRegistryPostProcessor(beanFactory) {
    ConfigurationClassPostProcessor.postProcessBeanDefinitionRegistry(registry) {
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
        
        processDeferredImportSelectors() {
          AutoConfigurationImportSelector.selectImports(annotationMetadata) {
            SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration.class, classLoader)
          }
          processImports(configClass, sourceClass, importClassName)
        }
      }
      ConfigurationClassParser.validate()
      ConfigurationClassBeanDefinitionReader.loadBeanDefinitions(configClasses) {
        TrackedConditionEvaluator.shouldSkip(configClass)
        _registerBeanDefinitionForImportedConfigurationClass(configClass)
        loadBeanDefinitionsForBeanMethod(configClass.getBeanMethods())
        loadBeanDefinitionsFromImportedResources(configClass.getImportedResources())
        loadBeanDefinitionsFromRegistrars(configClass.getImportBeanDefinitionRegistrars())
      }
      _processUntilNoNewCandidates()
      _registerSingleton(importRegistry)
    }
    invokeBeanFactoryPostProcessors(registryProcessors, beanFactory) {
      ConfigurationClassPostProcessor.postProcessBeanFactory(beanFactory) {
        enhanceConfigurationClasses(beanFactory) {
          new ConfigurationClassEnhancer()
          _enhance(enhancer, configClass, beanClassLoader)
        }
        new ImportAwareBeanPostProcessor()
        _addBeanPostProcessor(beanFactory, importAwareBeanPostProcessor)
      }
    }
  }
  _invokeBeanFactoryPostProcessor(beanFactory) {
    PropertySourcesPlaceholderConfigurer.postProcessBeanFactory(beanFactory) {
      _addEnvironmentPropertySource()
      _addLocalPropertySource()
    }
  }
}