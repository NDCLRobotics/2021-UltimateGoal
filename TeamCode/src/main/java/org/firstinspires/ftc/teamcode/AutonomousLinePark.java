package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;


@Autonomous(name = "Autonomous Line Park", group = "Concept")
//@Disabled
public class AutonomousLinePark extends LinearOpMode {
    // control hub 1
    private DcMotor frontLeftMotor = null;
    private DcMotor frontRightMotor = null;
    private DcMotor backLeftMotor = null;
    private DcMotor backRightMotor = null;
    private CRServo ringServo = null;
    private CRServo flipServo = null;
    private CRServo toggleServo = null;

    // control hub 2
    private DcMotor launchingMotor = null;
    private DcMotor wobbleMotor = null;

    // switching is a workaround to allow only one frame input
    private boolean powerSwitching = false;
    private boolean toggleSwitching = false;
    // launch motor related booleans
    private boolean launchSwitching = false;
    private boolean launchPresetSwitching = false;
    // when activated, the two control sticks will be inverted - currently unused
    private boolean inverseControls = false;

    // servo related booleans
    private boolean toggleBool = false;

    // scale down power from max for driving
    private double powerScale = 0.75;
    private double launchPowerScale = 0.5;
    private double highLaunchPowerScale = 0.9;

    // doubles for driving and panning
    private double frontLeftPower, frontRightPower, backLeftPower, backRightPower;
    private double frontLeftPan, frontRightPan, backLeftPan, backRightPan;

    // scale down power from max for panning
    private double panPower = powerScale + 0.2;

    // variables for wobble motor
    private double wobbleSpeed;
    private int wobbleMotorZero;
    private int wobbleLimit = 20000;

    private int lowWobLimit = -15;
    private int highWobLimit = 65;

    // temporary variable for testing
    private double tempTestingVariable = 0.0;

    // variables regarding time
    private long loopCount = 0;
    private long initTime;
    private long finalTime;

    // strings relating to the voids we make for functions of the robot
    private static final String D_FORWARD = "forward";
    private static final String D_BACKWARD = "backward";
    private static final String D_STOP = "stop";

    public void drive (String fb)
    {
        if (fb.equals(D_FORWARD))
        {
            frontLeftMotor.setPower(0.3);
            frontRightMotor.setPower(0.3);
            backLeftMotor.setPower(0.3);
            backRightMotor.setPower(0.3);
        }
        if (fb.equals(D_BACKWARD))
        {
            frontLeftMotor.setPower(-0.3);
            frontRightMotor.setPower(-0.3);
            backLeftMotor.setPower(-0.3);
            backRightMotor.setPower(-0.3);
        }
        if (fb.equals(D_STOP))
        {
            frontLeftMotor.setPower(0);
            frontRightMotor.setPower(0);
            backLeftMotor.setPower(0);
            backRightMotor.setPower(0);
        }
    }

    @Override
    public void runOpMode()
    {
        // control hub 1
        frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        backRightMotor = hardwareMap.dcMotor.get("backRightMotor");
        ringServo = hardwareMap.crservo.get("ringServo"); // ringservo originally
        flipServo = hardwareMap.crservo.get("flipServo"); // flipservo originally
        toggleServo = hardwareMap.crservo.get("toggleServo");

        // control hub 2
        launchingMotor = hardwareMap.dcMotor.get("launchingMotor");
        wobbleMotor = hardwareMap.dcMotor.get("wobbleMotor");


        //setting the direction for each motor
        frontLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        backLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        launchingMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        wobbleMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        wobbleMotorZero = wobbleMotor.getCurrentPosition();

        // message displayed on the phone before initialization
        telemetry.addData("say: ", "Working, latest updated: never lol");

        // initial claw position
        ringServo.setPower(0.00);

        telemetry.addData(">", "Press Play to start op mode");
        telemetry.update();
        waitForStart();

        if (opModeIsActive())
        {
            initTime = System.currentTimeMillis();
            while (opModeIsActive())
            {
                finalTime = System.currentTimeMillis() - initTime;
                loopCount += 1;

                telemetry.addData("Loop count:", +loopCount);
                telemetry.addData("Time is:", finalTime);
                telemetry.addData("Ms/loop", finalTime / loopCount);

                if (finalTime > 0 && finalTime < 3000)
                {
                    drive(D_FORWARD);
                }
                if (finalTime > 3000)
                {
                    drive(D_STOP);
                }
            }
        }
    }
}