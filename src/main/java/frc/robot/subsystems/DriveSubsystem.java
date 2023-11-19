package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.DriveConstants;

public class DriveSubsystem extends SubsystemBase {

    // The motors on the left side of the drive.
    private final TalonSRX      leftPrimaryMotor         = new TalonSRX(DriveConstants.LEFT_MOTOR_PORT);
    private final TalonSRX      leftFollowerMotor        = new TalonSRX(DriveConstants.LEFT_MOTOR_PORT + 1);

    // The motors on the right side of the drive.
    private final TalonSRX      rightPrimaryMotor        = new TalonSRX(DriveConstants.RIGHT_MOTOR_PORT);
    private final TalonSRX      rightFollowerMotor       = new TalonSRX(DriveConstants.RIGHT_MOTOR_PORT + 1);

    // The gyro sensor
    // FIXME I think we are going to add the gyro to the MXP expansion port, not the serial port
    private final AHRS          gyroSensorAhrs           = new AHRS(SerialPort.Port.kUSB1);

    // Ultrasonic sensor
    // Conversion from volts to distance in cm
    // Volts distance
    // 0.12 30.5 cm
    // 2.245 609.6 cm
    private final AnalogInput   ultrasonicDistanceSensor = new AnalogInput(0);

    private static final double ULTRASONIC_M             = (609.6 - 30.5) / (2.245 - .12);
    private static final double ULTRASONIC_B             = 609.6 - ULTRASONIC_M * 2.245;

    // Motor speeds
    private double              leftSpeed                = 0;
    private double              rightSpeed               = 0;

    /** Creates a new DriveSubsystem. */
    public DriveSubsystem() {

        // We need to invert one side of the drivetrain so that positive voltages
        // result in both sides moving forward. Depending on how your robot's
        // gearbox is constructed, you might have to invert the left side instead.
        leftPrimaryMotor.setInverted(DriveConstants.LEFT_MOTOR_REVERSED);
        leftFollowerMotor.setInverted(DriveConstants.LEFT_MOTOR_REVERSED);

        leftPrimaryMotor.setNeutralMode(NeutralMode.Brake);
        leftFollowerMotor.setNeutralMode(NeutralMode.Brake);

        leftFollowerMotor.follow(leftPrimaryMotor);


        rightPrimaryMotor.setInverted(DriveConstants.RIGHT_MOTOR_REVERSED);
        rightFollowerMotor.setInverted(DriveConstants.RIGHT_MOTOR_REVERSED);

        rightPrimaryMotor.setNeutralMode(NeutralMode.Brake);
        rightFollowerMotor.setNeutralMode(NeutralMode.Brake);

        rightFollowerMotor.follow(rightPrimaryMotor);

    }

    public double getUltrasonicDistanceCm() {

        double ultrasonicVoltage = ultrasonicDistanceSensor.getVoltage();

        // Use a straight line y = mx + b equation to convert voltage into cm.
        double distanceCm        = ULTRASONIC_M * ultrasonicVoltage + ULTRASONIC_B;

        return Math.round(distanceCm);
    }

    /**
     * Set the left and right speed of the primary and follower motors
     *
     * @param leftSpeed
     * @param rightSpeed
     */
    public void setMotorSpeeds(double leftSpeed, double rightSpeed) {

        this.leftSpeed  = leftSpeed;
        this.rightSpeed = rightSpeed;

        leftPrimaryMotor.set(ControlMode.PercentOutput, leftSpeed);
        rightPrimaryMotor.set(ControlMode.PercentOutput, rightSpeed);

        // NOTE: The follower motors are set to follow the primary motors
    }

    /** Safely stop the subsystem from moving */
    public void stop() {
        setMotorSpeeds(0, 0);
    }

    @Override
    public void periodic() {

        /*
         * Update all dashboard values in the periodic routine
         */
        SmartDashboard.putNumber("Right Motor", rightSpeed);
        SmartDashboard.putNumber("Left  Motor", leftSpeed);

        // Round the ultrasonic voltage to 2 decimals
        SmartDashboard.putNumber("Ultrasonic Voltage",
            Math.round(ultrasonicDistanceSensor.getVoltage() * 100.0d) / 100.0d);
        SmartDashboard.putNumber("Ultrasonic Distance (cm)", getUltrasonicDistanceCm());

        // Gets the yaw from the gyro sensor
        SmartDashboard.putNumber("Gyro Yaw", getYawReal());

    }

    @Override
    public String toString() {

        // Create an appropriate text readable string describing the state of the subsystem
        StringBuilder sb = new StringBuilder();

        sb.append(this.getClass().getSimpleName())
            .append(" [").append(Math.round(leftSpeed * 100.0d) / 100.0d)
            .append(',').append(Math.round(rightSpeed * 100.0d) / 100.0d).append(']')
            .append(" ultrasonic dist ").append(getUltrasonicDistanceCm());

        return sb.toString();

    }

    // returns the yaw
    // FIXME What does Real mean?
    // Maybe this should return a compass heading instead of the yaw value.
    // Instead of measurements like -185, it might be better to have that show in
    // the range of 0-360 degrees. As the robot spins, the counter keeps going up (or down)
    // so you can end up with raw angles of -745deg (which is not particularly human readable).
    // Try to round the compass heading to .1 deg increments - we do not need a compass heading
    // with 4 decimal places.
    public float getYawReal() {
        return gyroSensorAhrs.getYaw();
    }

}