package messages

import models.{BuyingRecommendation, PerformanceIndicators, SellingRecommendation}

sealed trait RecommenderMessage

final case class LatestIndicators(table: Map[String, PerformanceIndicators]) extends RecommenderMessage

final case class PushRecommendations(buyingRecommendation: Seq[BuyingRecommendation], sellingRecommendation: Seq[SellingRecommendation]) extends RecommenderMessage

final case class LogError(exception: Throwable) extends RecommenderMessage