package ACS;

import com.sun.javafx.scene.control.skin.VirtualFlow;

import java.util.*;

/**
 * Created by Jakub on 06.09.2016.
 *
 * Provides fuzzy-logic regulator functionality for a basic balancing arm with two thrusters.
 */
public class FuzzyRegulatorNonCOG {

    private double minThrust;
    private double maxThrust;
    private double recommendedLeft;
    private double recommendedRight;
    private double lastError;
    private List<Rule> recentlyUsedRules;
    private List<Rule> rules;

    private HashMap<Integer, MembershipFunction> angleError;
    private HashMap<Integer, MembershipFunction> angularVel;
    private HashMap<Integer, MembershipFunction> thrReco;

// =====================================================================================================================

    public FuzzyRegulatorNonCOG(double minThrust, double maxThrust, List<Integer> reactions){
        this.minThrust = minThrust;
        this.maxThrust = maxThrust;
        this.recommendedLeft = 0.0;
        this.recommendedRight = 0.0;
        this.lastError = 0.0;
        this.recentlyUsedRules = new LinkedList<>();

        // prepare membership functions for input arguments
        angleError = new HashMap<>();
        angleError.put(4, new MembershipFunction(false, true, 1.0 / 15.0, 60.0));
        angleError.put(3, new MembershipFunction(false, false, 1.0 / 15.0, 45.0));
        angleError.put(2, new MembershipFunction(false, false, 1.0 / 15.0, 25.0));
        angleError.put(1, new MembershipFunction(false, false, 1.0 / 10.0, 10.0));
        angleError.put(0, new MembershipFunction(false, false, 1.0 / 10.0, 0.0));
        angleError.put(-1, new MembershipFunction(false, false, 1.0 / 10.0, -10.0));
        angleError.put(-2, new MembershipFunction(false, false, 1.0 / 15.0, -25.0));
        angleError.put(-3, new MembershipFunction(false, false, 1.0 / 15.0, -45.0));
        angleError.put(-4, new MembershipFunction(true, false, 1.0 / 15.0, -60.0));

        angularVel = new HashMap<>();
        angularVel.put(4, new MembershipFunction(false, true, 1.0 / 7.5, 30.0));
        angularVel.put(3, new MembershipFunction(false, false, 1.0 / 7.5, 22.5));
        angularVel.put(2, new MembershipFunction(false, false, 1.0 / 7.5, 12.5));
        angularVel.put(1, new MembershipFunction(false, false, 1.0 / 5.0, 5.0));
        angularVel.put(0, new MembershipFunction(false, false, 1.0 / 5.0, 0.0));
        angularVel.put(-1, new MembershipFunction(false, false, 1.0 / 5.0, -5.0));
        angularVel.put(-2, new MembershipFunction(false, false, 1.0 / 7.5, -12.5));
        angularVel.put(-3, new MembershipFunction(false, false, 1.0 / 7.5, -22.5));
        angularVel.put(-4, new MembershipFunction(true, false, 1.0 / 7.5, -30.0));

        // prepare membership functions for output thrust recommendations
        double span = maxThrust - minThrust;
        double factor = span / 8.0;
        thrReco = new HashMap<>();
        thrReco.put(8, new MembershipFunction(false, false, factor, maxThrust));
        thrReco.put(7, new MembershipFunction(false, false, factor, minThrust + (span / 8.0) * 7));
        thrReco.put(6, new MembershipFunction(false, false, factor, minThrust + (span / 8.0) * 6));
        thrReco.put(5, new MembershipFunction(false, false, factor, minThrust + (span / 8.0) * 5));
        thrReco.put(4, new MembershipFunction(false, false, factor, minThrust + (span / 8.0) * 4));
        thrReco.put(3, new MembershipFunction(false, false, factor, minThrust + (span / 8.0) * 3));
        thrReco.put(2, new MembershipFunction(false, false, factor, minThrust + (span / 8.0) * 2));
        thrReco.put(1, new MembershipFunction(false, false, factor, minThrust + (span / 8.0) * 1));
        thrReco.put(0, new MembershipFunction(false, false, factor, minThrust));

        // prepare rule base
        int z = 0;

        for(int verse = 0; verse < 9; verse++) {
            for(int column = 0; column < 9; column++) {
                rules.add(new Rule(verse - 4, column - 4, reactions.get(z)));
                z++;
            }
        }

    }

// =====================================================================================================================

    public void calculateNewThrusts(double angle, double angularVelocity, double desiredAngle){
        lastError = desiredAngle - angle;
        recentlyUsedRules.clear();

        // fuzzyfication of input
        List<FuzzyInput> fuzzyAngle = new LinkedList<>();

        for(Integer angleErrIndex : angleError.keySet()) {
            double pertinence = angleError.get(angleErrIndex).getMembershipFactor(angle);
            if(pertinence > 0.0)
                fuzzyAngle.add(new FuzzyInput(angleErrIndex, pertinence));
        }

        List<FuzzyInput> fuzzyVel = new LinkedList<>();

        for(Integer velErrIndex : angularVel.keySet()) {
            double pertinence = angularVel.get(velErrIndex).getMembershipFactor(angularVelocity);
            if(pertinence > 0.0)
                fuzzyVel.add(new FuzzyInput(velErrIndex, pertinence));
        }

        // gather recommendations from rules and save used rules
        List<Recommendation> recommendations = new LinkedList<>();

        // check all combinations of active input levels to determine which rules are on
        for(Rule r : rules) {
            for(FuzzyInput ang : fuzzyAngle) {
                for(FuzzyInput vel : fuzzyVel) {
                    if(r.getAngle() == ang.getInputValue() && r.getAngularVelocity() == vel.getInputValue()) {
                        // rule is on
                        recommendations.add(new Recommendation(r.getRecommendedThrust(),
                                Math.min(ang.getPertinence(), vel.getPertinence())));

                        recentlyUsedRules.add(new Rule(ang.getInputValue(), vel.getInputValue(),
                                r.getRecommendedThrust()));
                    }
                }
            }
        }

        // deffuzyfication
        double totalVal = 0.0;
        double totalPertinence = 0.0;

        for(Recommendation reco : recommendations) {
            double abs_rec = Math.abs(reco.getRecommendedThrust());
            double center = thrReco.get(abs_rec).getCentralBoundary();

            if(reco.getRecommendedThrust() >= 0)
                totalVal += center * reco.getPertinence();      //< left thruster active
            else
                totalVal -= center * reco.getPertinence();      //< right thruster active

            totalPertinence += reco.getPertinence();
        }

        // center-average calculation
        double finalRecommendation = totalVal / totalPertinence;

        // save results to proper fields
        if(finalRecommendation == 0.0) {        //< thrusters disabled
            recommendedRight = minThrust;
            recommendedLeft = minThrust;
        }
        else if(finalRecommendation > 0) {      //< left thruster active
            recommendedLeft = finalRecommendation;
            recommendedRight = minThrust;
        }
        else if(finalRecommendation < 0) {      //< right thruster active
            recommendedLeft = minThrust;
            recommendedRight = -finalRecommendation;
        }

    }

// =====================================================================================================================

    public double getRightThrust() {
        return recommendedRight;
    }

    public double getLeftThrust() {
        return recommendedLeft;
    }

    public double getLastErr() {
        return lastError;
    }

// =====================================================================================================================

    // ##############################################
    //              UTILITY CLASSES                 #
    // ##############################################


    private class Recommendation{
        private int recommendedThrust;
        private double pertinence;

        public Recommendation(int recommendedThrust, double pertinence){
            this.recommendedThrust = recommendedThrust;
            this.pertinence = pertinence;
        }

        public int getRecommendedThrust(){
            return recommendedThrust;
        }

        public double getPertinence(){
            return pertinence;
        }
    }


    private class FuzzyInput{
        private int inputValue;
        private double pertinence;

        public FuzzyInput(int inputValue, double pertinence){
            this.inputValue = inputValue;
            this.pertinence = pertinence;
        }

        public int getInputValue(){
            return inputValue;
        }

        public double getPertinence(){
            return pertinence;
        }
    }

    private class Rule{
        private int angle;
        private int angularVelocity;
        private int recommendedThrust;

        public Rule(int angle, int angularVelocity, int recommendedThrust){
            this.angle = angle;
            this.angularVelocity = angularVelocity;
            this.recommendedThrust = recommendedThrust;
        }

        public int getAngle(){
            return angle;
        }

        public int getAngularVelocity(){
            return angularVelocity;
        }

        public int getRecommendedThrust(){
            return recommendedThrust;
        }
    }
}
