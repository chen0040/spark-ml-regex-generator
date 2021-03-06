package com.github.chen0040.spark.regp;


import com.github.chen0040.sparkml.commons.SparkContextFactory;
import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import org.apache.spark.api.java.JavaSparkContext;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;


/**
 * Created by xschen on 23/6/2017.
 */
public class GpCultivatorUnitTest {

   @Test
   public void test_find_ip(){
      GpCultivator generator = new GpCultivator();
      generator.setDisplayEvery(2);
      generator.setPopulationSize(100);
      generator.setMaxGenerations(50);

      List<String> trainingData = new ArrayList<>();
      trainingData.add("user root login at 127.0.0.1");

      JavaSparkContext context = SparkContextFactory.createSparkContext("testing-1");

      Grok generated_grok = generator.fit(context.parallelize(trainingData));

      System.out.println("user root login at 127.0.0.1");
      System.out.println(generator.getRegex());
      //System.out.println(generator.getSolution().mathExpression());


      Match matched = generated_grok.match("user root login at 127.0.0.1");
      matched.captures();
      System.out.println(matched.toJson());



   }
}
