package frc.robot.commands.climb;

import frc.robot.Constants.ClimbConstants;
import frc.robot.operator.OperatorInput;
import frc.robot.subsystems.ClimbSubsystem;

public class LowerBothClimbersCommand extends BaseClimberCommand {

    OperatorInput operatorInput;

    public LowerBothClimbersCommand(ClimbSubsystem climbSubsystem, OperatorInput operatorInput) {
        super(climbSubsystem);
        this.operatorInput = operatorInput;
    }

    @Override
    public void initialize() {
        logCommandStart();
        super.initialize();
    }

    // Check here
    @Override
    public void execute() {
        climbSubsystem.setLeftSpeed(ClimbConstants.CLIMBER_LOWER_SPEED);
        climbSubsystem.setRightSpeed(ClimbConstants.CLIMBER_LOWER_SPEED);
    }

    @Override
    public boolean isFinished() {

        if (!operatorInput.isLowerClimbers() && isTimeoutExceeded(0.25)) {
            setFinishReason("let go of intake button");
            return true;
        }

        return false;
    }

    @Override
    public void end(boolean interrupted) {
        logCommandEnd(interrupted);
    }

}
