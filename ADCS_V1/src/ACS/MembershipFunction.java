package ACS;

/**
 * Created by Jakub on 06.09.2016.
 * Encapsulates a membership function used by fuzzy logic.
 */
public class MembershipFunction{

    private double slopeFactor;
    private double centralBoundary;
    private double leftBoundary;
    private double rightBoundary;
    private boolean leftSaturation;
    private boolean rightSaturation;

    public MembershipFunction(boolean leftSaturation, boolean rightSaturation, double slopeFactor,
                              double centralBoundary){
        this.leftSaturation = leftSaturation;
        this.rightSaturation = rightSaturation;
        this.centralBoundary = centralBoundary;
        this.slopeFactor = slopeFactor;

        this.leftBoundary = this.centralBoundary - 1.0 / this.slopeFactor;
        this.rightBoundary = this.centralBoundary + 1.0 / this.slopeFactor;
    }

    public double getMembershipFactor(double value){
        if(value == centralBoundary) {                                                  //< peak
            return 1.0;
        }
        else if(value < centralBoundary && value > leftBoundary && !leftSaturation){    //< left zone - no saturation
            return slopeFactor * (value - leftBoundary);
        }
        else if(value < centralBoundary && value > leftBoundary && leftSaturation){     //< left zone - saturation
            return 1.0;
        }
        else if(value > centralBoundary && value < rightBoundary && !rightSaturation){  //< right zone - no saturation
            return -slopeFactor * (value - rightBoundary);
        }
        else if(value > centralBoundary && value < rightBoundary && rightSaturation){   //< right zone - saturation
            return 1.0;
        }
        else if(value <= leftBoundary && leftSaturation){                               //< beyond left - saturation
            return 1.0;
        }
        else if(value <= leftBoundary && !leftSaturation){                              //< beyond left - no saturation
            return 0.0;
        }
        else if(value >= rightBoundary && rightSaturation){                             //< beyond right - saturation
            return 1.0;
        }
        else if(value >= rightBoundary && !rightSaturation){                            //< beyond right - no saturation
            return 0.0;
        }
        else
            return 0.0;
    }

    public double getCentralBoundary() {
        return centralBoundary;
    }

    public double getSlopeFactor() {
        return slopeFactor;
    }

    public double getLeftBoundary() {
        return leftBoundary;
    }

    public double getRightBoundary() {
        return rightBoundary;
    }
}
