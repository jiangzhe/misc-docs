SpringApplication.prepareEnvironment() {
  StandardServletEnvironment.customizePropertySources() {
    addLast(systemPropertiesPropertySource)
    addLast(systemEnvironmentPropertySource)
  }
  configureProfiles()
  SpringApplicationRunListener.environmentPrepared() {
    ApplicationEventMulticaster.multicastEvent(event) {
      ApplicationListener.onApplicationEvent(event) {
        ConfigFileApplicationListener.onApplicationEnvironmentPreparedEvent(event) {
          Loader.load() {
            PropertySourceLoader.load(name, resource)
          }
        }
      }
    }
  }
}
