if [ ! "$ENV" ]; then
    echo "NO ENVIRONMENT NAME IS SET! SETTINGS \$ENV TO TEST";
    ENV=TEST;
fi;

CLASSPATH=$HOME/.m2/repository/dom4j/dom4j/1.6.1/dom4j-1.6.1.jar:$HOME/.m2/repository/org/apache/activemq/activemq-all/5.3.2/activemq-all-5.3.2.jar:target/bitmagasin-integrationtest-0.0.1-SNAPSHOT.jar
java -cp $CLASSPATH dk.bitmagasin.pillar.MockupPillar timeoutMeasure=1 timeoutUnit=SECONDS pillarId=KB environmentName=$ENV > kb_$ENV.log 2>&1 &
java -cp $CLASSPATH dk.bitmagasin.pillar.MockupPillar timeoutMeasure=2 timeoutUnit=MINUTES pillarId=SB environmentName=$ENV > sb_$ENV.log 2>&1 &
java -cp $CLASSPATH dk.bitmagasin.pillar.MockupPillar timeoutMeasure=5 timeoutUnit=DAYS pillarId=SA errorCode=2 "errorMessage=Cannot find file" environmentName=$ENV > sa_$ENV.log 2>&1 &
java -cp $CLASSPATH dk.bitmagasin.pillar.MockupPillar timeoutMeasure=-1 timeoutUnit=NEVER pillarId=UNKNOWN environmentName=$ENV > unknown_$ENV.log 2>&1 &

sleep 5;

java -cp $CLASSPATH dk.bitmagasin.client.MockupClient clientQueue=ClientCues "action=GetTime->SB,KB,SA" "action=GetData->KB" environmentName=$ENV > client_$ENV.log 2>&1 &

sleep 5;

cat *1.log
