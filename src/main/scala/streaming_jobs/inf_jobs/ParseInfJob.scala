package streaming_jobs.inf_jobs

import com.typesafe.config.{Config, ConfigFactory}
import core.sources.KafkaDStreamSource
import core.streaming.{SparkLogLevel, SparkStreamingApplication}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.streaming.dstream.DStream

import scala.concurrent.duration.FiniteDuration

/**
  * Created by hungdv on 19/06/2017.
  */
class ParseInfJob(config: InfParserConfig,source: KafkaDStreamSource)extends SparkStreamingApplication{
  override def streamingBatchDuration: FiniteDuration = config.streamingBatchDurations

  override def streamingCheckpointDir: String = config.streamingCheckPointDir

  override def sparkConfig: Map[String, String] = config.sparkConfig

  def start(): Unit = {
    withSparkStreamingContext { (ss, ssc) =>
      val input: DStream[String] = source.createSource(ssc, config.inputTopic)
      val infParser = new parser.INFLogParser
      ParseAndSaveInfV2.parseAndSave(
        ssc,ss,input,infParser,config.postgresConfig,config.infPortDownKafkaTopic,config.producerConfig,
        "172.27.11.173:6379,172.27.11.175:6379,172.27.11.176:6379,172.27.11.173:6380,172.27.11.175:6380,172.27.11.176:6380"
      )
    }
  }
}


class DetectPatternErro(config: InfDisconectConfig,source: KafkaDStreamSource)extends SparkStreamingApplication{
  override def streamingBatchDuration: FiniteDuration = config.streamingBatchDurations

  override def streamingCheckpointDir: String = config.streamingCheckPointDir

  override def sparkConfig: Map[String, String] = config.sparkConfig

  def start(): Unit = {
    withSparkStreamingContext { (ss, ssc) =>
      val input: DStream[String] = source.createSource(ssc, config.infPortDownKafkaTopic)
      PatternModuleDetect.parseAndSave(
        ssc,ss,input,config.postgresConfig
      )
    }
  }
}


object InfDetectJob{
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
    SparkLogLevel.setStreamingLogLevels()
    val config = InfDisconectConfig()
    val infJob = new DetectPatternErro(config,KafkaDStreamSource(config.souceKafka))
    infJob.start()
  }
}


object InfJob{
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
    SparkLogLevel.setStreamingLogLevels()
    val config = InfParserConfig()
    val infJob = new ParseInfJob(config,KafkaDStreamSource(config.souceKafka))
    infJob.start()
  }
}




class DetectPatternLos(config: InfLosConfig,source: KafkaDStreamSource)extends SparkStreamingApplication{
  override def streamingBatchDuration: FiniteDuration = config.streamingBatchDurations

  override def streamingCheckpointDir: String = config.streamingCheckPointDir

  override def sparkConfig: Map[String, String] = config.sparkConfig

  def start(): Unit = {
    withSparkStreamingContext { (ss, ssc) =>
      val input: DStream[String] = source.createSource(ssc, config.infLosKafkaTopic)
      LosOltDetect.run(
        ssc,ss,input,config.postgresConfig
      )
    }
  }
}


object DetectPatternLos{
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
    SparkLogLevel.setStreamingLogLevels()
    val config = InfLosConfig()
    val infJob = new DetectPatternLos(config,KafkaDStreamSource(config.souceKafka))
    println("Start -----")
    infJob.start()
  }
}


















