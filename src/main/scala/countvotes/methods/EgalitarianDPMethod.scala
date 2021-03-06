package countvotes.methods

import countvotes.structures._
import countvotes.algorithms._
import scala.math._
import scala.collection.mutable.{HashMap => MMap}

object EgalitarianDPMethod extends Egalitarian[WeightedBallot] {
  val memo = new MMap[(Int,Set[Candidate]), List[Candidate]] ()
  var allCandidates: List[Candidate] = List.empty

  def runScrutiny(election: Election[WeightedBallot], candidates: List[Candidate], numVacancies: Int):  Report[WeightedBallot] = {
    allCandidates = candidates
    println("Number of WeightedBallots: " + election.length)
    report.setWinners(winners(election, candidates, numVacancies))
    report
  }

  def winners(election: Election[WeightedBallot],  ccandidates: List[Candidate], numVacancies: Int): List[(Candidate,Rational)] = {
    val candidateCount: Int = allCandidates.length
    if(candidateCount < numVacancies) {println("not enough candidates") }

    var winningCandidates: List[Candidate] = List.empty
    winningCandidates = recursiveWinnersComputation(ccandidates, numVacancies, election)

    var candidatesForReturn : List[(Candidate,Rational)] = List.empty
    for(i <- winningCandidates){
      candidatesForReturn = candidatesForReturn :+ (i,new Rational(n=1,d=1))
    }
    candidatesForReturn
  }

  def recursiveWinnersComputation(candidateList: List[Candidate], numVacancies: Int, election: Election[WeightedBallot]): List[Candidate] = numVacancies match {
    case 0 => List.empty
    case n => {
      if(memo.contains((numVacancies,candidateList.toSet))){
        memo((numVacancies,candidateList.toSet))
      }
      var contemplatedSets : List[List[Candidate]] = List.empty
      for(i <- candidateList){
        contemplatedSets = contemplatedSets :+ (recursiveWinnersComputation(candidateList.filterNot(elem => elem == i), numVacancies-1, election) :+ i)
      }
      val contemplatedSetsWelfareTuple: List[(Double,List[Candidate])] = contemplatedSets.map(x => (socialWelfare(election, x),x))
      val result: List[Candidate] = (contemplatedSetsWelfareTuple.foldLeft ((0.0, candidateList)) ((x,y) => maxTuple1(x,y)))._2   //Error if all less than 0
      memo += (((numVacancies,candidateList.toSet),result))
      result
    }
  }
}
