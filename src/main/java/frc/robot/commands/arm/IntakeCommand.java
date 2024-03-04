package frc.robot.commands.arm;

import frc.robot.Constants.ArmConstants;
import frc.robot.Constants.ArmConstants.HeadingStates;
import frc.robot.Constants.ArmConstants.IntakeStates;
import frc.robot.commands.LoggingCommand;
import frc.robot.operator.OperatorInput;
import frc.robot.subsystems.ArmSubsystem;

public class IntakeCommand extends LoggingCommand {

    ArmSubsystem          armSubsystem;
    OperatorInput         operatorInput;

    // PivotPID
    private double        currentError;
    private double        previousError;
    private double        diffError;
    private double        errorSignal;
    private double        pTerm;
    private double        iTerm       = 0;
    private double        dTerm;

    // Speeds
    private double        intakeSpeed;
    private double        pivotSpeed;

    // Time Measure
    private double        initTime;
    private double        currTime;
    private double        timeoutMS;

    // Logging skibidi
    private double        speed;
    private double        targetAngle = 0;
    private String        reason;

    // States
    private IntakeStates  state;
    private HeadingStates headingState;



    public IntakeCommand(double intakeSpeed, double pivotSpeed, double timeoutMS, ArmSubsystem armSubsystem) {

        this.intakeSpeed  = intakeSpeed;
        this.pivotSpeed   = pivotSpeed;
        this.timeoutMS    = timeoutMS;
        this.armSubsystem = armSubsystem;

        addRequirements(armSubsystem);

    }

    @Override
    public void initialize() {

        String commandParms = "target angle: " + targetAngle + "intake speed: " + intakeSpeed + "pivot speed: " + pivotSpeed
            + "pivot speed: " + pivotSpeed
            + ", timeout time (ms): " + timeoutMS;
        logCommandStart(commandParms);

        state         = IntakeStates.PIVOTING;

        previousError = armSubsystem.getAngleErrorPivot(targetAngle);
        initTime      = System.currentTimeMillis();

        if (Math.abs(previousError) > 10) {
            headingState = HeadingStates.FAR;
        }
        else {
            headingState = HeadingStates.CLOSE;
        }

    }

    @Override
    public void execute() {

        switch (state) {

        case PIVOTING:

            currentError = armSubsystem.getAngleErrorPivot(targetAngle);
            diffError = currentError - previousError;
            previousError = currentError;

            double sgnError = Math.abs(currentError) / currentError;

            switch (headingState) {

            case FAR:
            default:

                armSubsystem.pivotRotSetSpeed(speed * sgnError);
                break;

            case CLOSE:

                pTerm = ArmConstants.PIVOT_TO_ANGLE_PID_KP * currentError;
                iTerm += ArmConstants.PIVOT_TO_ANGLE_PID_KI * currentError;
                dTerm += ArmConstants.PIVOT_TO_ANGLE_PID_KD * diffError;

                errorSignal = pTerm + iTerm + dTerm;

                Math.max(Math.min(errorSignal + Math.abs(errorSignal) / errorSignal * 0.2, 1), -1);

                armSubsystem.pivotRotSetSpeed(speed * sgnError);

                break;
            }

            if (Math.abs(previousError) > ArmConstants.PIVOT_FAR_TO_CLOSE) {
                headingState = HeadingStates.FAR;
            }
            else {
                headingState = HeadingStates.CLOSE;
            }

            break;

        case INTAKING:

            armSubsystem.intakeSetSpeed(speed);

            break;


        }

    }

    @Override
    public boolean isFinished() {

        currTime = System.currentTimeMillis();

        if (currTime - initTime >= timeoutMS) {
            reason = "timeout of " + timeoutMS + " ms extends";
            return true;
        }

        if (state == IntakeStates.PIVOTING) {
            if (Math.abs(currentError) <= ArmConstants.PIVOT_ROT_BUFFER) {
                state = IntakeStates.INTAKING;
                armSubsystem.pivotRotSetSpeed(0);
            }
        }

        if (!operatorInput.isIntake()) {
            reason = "let go of intake button";
            return true;
        }

        return false;


    }

    @Override
    public void end(boolean interrupted) {

        armSubsystem.intakeSetSpeed(0);
        armSubsystem.pivotRotSetSpeed(0);


        setFinishReason(reason);
        logCommandEnd(interrupted);

    }



}
