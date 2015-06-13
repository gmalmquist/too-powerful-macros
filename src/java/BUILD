jvm_binary(name='java',
  dependencies=[':lib', 'src/antlr'],
  main='com.gmalmquist.tpm.Main',
  basename='tpm',
)

java_library(name='lib',
  sources=rglobs('*.java'),
  dependencies=['//:antlr-4'],
)


