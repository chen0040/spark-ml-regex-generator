package com.github.chen0040.spark.regp;


import com.github.chen0040.gp.commons.BasicObservation;
import com.github.chen0040.gp.commons.Observation;
import com.github.chen0040.gp.treegp.TreeGP;
import com.github.chen0040.gp.treegp.program.Solution;
import com.github.chen0040.spark.regp.operators.Concat;
import com.github.chen0040.sparkml.gp.SparkTreeGP;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.exception.GrokException;
import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xschen on 22/6/2017.
 */
@Getter
@Setter
public class GpCultivator {

   private int displayEvery = -1;
   private int populationSize = 1000;
   private int maxGenerations = 100;
   private String regex = null;
   private Solution solution = null;
   private Grok grok = null;
   private static final Logger logger = LoggerFactory.getLogger(GpCultivator.class);

   @Setter(AccessLevel.NONE)
   private SparkTreeGP treeGP;

   public Grok fit(JavaRDD<String> trainingData) {
      treeGP= new SparkTreeGP();
      treeGP.setDisplayEvery(displayEvery);
      treeGP.setPopulationSize(populationSize);
      treeGP.setMaxGeneration(maxGenerations);
      treeGP.setVariableCount(0);

      treeGP.getOperatorSet().addAll(new Concat());
      treeGP.setPerObservationCostEvaluator(tuple2 -> {

         Solution p = tuple2._1();

         double cost = 0;
         GrokObservation observation = (GrokObservation) tuple2._2();
         p.executeWithText(observation);
         String regex = observation.getPredictedTextOutput(0);
         cost += GrokService.evaluate(regex, observation.getText());
         return cost;
      });

      int patternCount = GrokService.countPatterns();
      for(int i= 0; i < patternCount; ++i) {
         treeGP.addConstant("%{" + GrokService.getPattern(i) + "}", 1.0);
      }

      JavaRDD<BasicObservation> rdd = trainingData.map(data -> {
         GrokObservation observation = new GrokObservation();
         observation.setText(data);
         return observation;
      });

      GrokObservation first = (GrokObservation) rdd.first();
      solution = treeGP.fit(rdd);

      solution.executeWithText(first);
      regex = first.getPredictedTextOutput(0);
      try {
         grok = GrokService.build(regex);
      }
      catch (GrokException e) {
         logger.error("Failed to build grok from regex " + regex, e);
      }

      return grok;
   }
}
