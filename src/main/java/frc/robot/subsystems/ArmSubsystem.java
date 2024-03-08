package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel;
import com.revrobotics.CANSparkMax;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ArmConstants;
import frc.robot.Constants.OperatorConstants;
import frc.robot.operator.GameController;


public class ArmSubsystem extends SubsystemBase {


    CANSparkMax                 pivot            = new CANSparkMax(ArmConstants.PIVOT_PORT,
        CANSparkLowLevel.MotorType.kBrushless);
    WPI_TalonSRX                intakeLower      = new WPI_TalonSRX(ArmConstants.INTAKE_LOWER_PORT);
    WPI_TalonSRX                intakeHigher     = new WPI_TalonSRX(ArmConstants.INTAKE_HIGHER_PORT);
    DigitalInput                proximitySensor  = new DigitalInput(ArmConstants.PROXIMITY_PORT);

    boolean                     loaded;

    double                      currAnglePivot;
    double                      encoderCountPivot;

    double                      pivotRotSpeed;
    double                      intakeLowerSpeed;
    double                      intakeHigherSpeed;


    public final GameController driverController = new GameController(
        OperatorConstants.DRIVER_CONTROLLER_PORT,
        OperatorConstants.GAME_CONTROLLER_STICK_DEADBAND);


    public ArmSubsystem() {

        this.currAnglePivot = getAnglePivot();

        pivot.setIdleMode(IdleMode.kBrake);
        pivot.setInverted(ArmConstants.PIVOT_INVERTED);


        intakeLower.setNeutralMode(NeutralMode.Coast);
        intakeHigher.follow(intakeHigher);


    }

    @Override
    public void periodic() {

        SmartDashboard.putNumber("Pivot Motor Speed", this.pivotRotSpeed);
        SmartDashboard.putNumber("Lower Intake Motor Speed", this.intakeLowerSpeed);
        SmartDashboard.putNumber("Higher Intake Motor Speed", this.intakeHigherSpeed);

        SmartDashboard.putNumber("Pivot encoder count", this.encoderCountPivot);
        SmartDashboard.putNumber("Arm Angle", this.currAnglePivot);

        SmartDashboard.putBoolean("Loaded", this.loaded);



    }

    // pivot methods

    public double getEncoderCountPivot() {
        this.encoderCountPivot = pivot.getAlternateEncoder(ArmConstants.PIVOT_ARM_ENCODER_COUNT_PER_ROTATION).getPosition();
        return this.encoderCountPivot;
    }

    public double getAnglePivot() {
        this.currAnglePivot = getEncoderCountPivot() * ArmConstants.PIVOT_ARM_ENCODER_COUNT_PER_ROTATION / 360;
        return this.currAnglePivot;
    }

    public double getAngleErrorPivot(double targetAngle) {
        return targetAngle - getAnglePivot();
    }

    public void pivotRotSetSpeed(double speed) {
        this.pivotRotSpeed = speed;
        pivot.set(pivotRotSpeed);
    }

    // intake methods

    public void intakeSetSpeed(double speed) {
        this.intakeLowerSpeed  = speed;
        this.intakeHigherSpeed = speed;
        intakeLower.set(speed);
        intakeHigher.set(speed);
    }

    // proximity sensor methods

    public boolean isLoaded() {
        this.loaded = proximitySensor.get();
        return loaded;
    }


}

