# change this to point to your local AppEngine Java SDK
export APPENGINE_SDK_HOME=~/bin/appengine-java-sdk-1.6.1

java -Dfile.encoding=UTF8 -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -jar `dirname $0`/sbt-launch.jar "$@"
