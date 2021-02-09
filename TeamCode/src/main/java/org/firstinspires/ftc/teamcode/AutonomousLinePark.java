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

    private boolean switching = true;
    private long switchTime = 0;

    // strings relating to the voids we make for functions of the robot
    private static final String D_FORWARD = "forward";
    private static final String D_BACKWARD = "backward";
    private static final String D_STOP = "stop";
    private static final String P_LEFT = "pan left";
    private static final String P_RIGHT = "pan right";
    private static final String P_STOP = "pan stop";
    private static final String T_LEFT = "turn left";
    private static final String T_RIGHT = "turn right";
    private static final String T_STOP = "turn stop";
    private static final String W_UP = "wobble up";
    private static final String W_DOWN = "wobble down";
    private static final String W_STOP = "wobble stop";

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

    public void pan (String lr)
    {
        if(lr.equals(P_LEFT))
        {
            frontLeftMotor.setPower(-0.3);
            frontRightMotor.setPower(0.3);
            backLeftMotor.setPower(0.3);
            backRightMotor.setPower(-0.3);
        }
        if(lr.equals(P_RIGHT))
        {
            frontLeftMotor.setPower(0.3);
            frontRightMotor.setPower(-0.3);
            backLeftMotor.setPower(-0.3);
            backRightMotor.setPower(0.3);
        }
        if(lr.equals(P_STOP))
        {
            frontLeftMotor.setPower(0);
            frontRightMotor.setPower(0);
            backLeftMotor.setPower(0);
            backRightMotor.setPower(0);
        }
    }
    public void turn (String lr)
    {
        if(lr.equals(T_LEFT))
        {
            frontLeftMotor.setPower(-0.3);
            frontRightMotor.setPower(0.3);
            backLeftMotor.setPower(-0.3);
            backRightMotor.setPower(0.3);
        }
        if(lr.equals(T_RIGHT))
        {
            frontLeftMotor.setPower(0.3);
            frontRightMotor.setPower(-0.3);
            backLeftMotor.setPower(0.3);
            backRightMotor.setPower(-0.3);
        }
        if(lr.equals(T_STOP))
        {
            frontLeftMotor.setPower(0);
            frontRightMotor.setPower(0);
            backLeftMotor.setPower(0);
            backRightMotor.setPower(0);
        }
    }
    public void wobble (String lr)
    {
        if(lr.equals(W_UP))
        {
            wobbleMotor.setPower(-0.55);
        }
        if(lr.equals(W_DOWN))
        {
           wobbleMotor.setPower(0.55);
        }
        if(lr.equals(W_STOP))
        {
            wobbleMotor.setPower(0.0);
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
            launchingMotor.setPower(0.6);

            while (opModeIsActive())
            {
                finalTime = System.currentTimeMillis() - initTime;
                loopCount += 1;

                telemetry.addData("Loop count:", +loopCount);
                telemetry.addData("Time is:", finalTime);
                telemetry.addData("Ms/loop", finalTime / loopCount);




                // AUTONOMOUS TESTING LOG
                // first trial: with left wheel directly on starting line
                // results: turned instead of panning (bruh); 1000 ms intervals could likely be shortened;
                //          spacing based on initial forward movement is correct; timing for launches using toggle are correct


                int drive1 = 6250;
                int wobbleDown1 = 300;
                int launch1 = drive1 + 1250;
                int reposition1 = launch1 + 800;
                int launch2 = reposition1 + 1100;
                int reposition2 = launch2 + 1000;
                int launch3 = reposition2 + 500;
                int drive2 = launch3 + 1600;
                int stop1 = drive2 + 200;
                int pan1 = stop1 + 2500;
                int turn1 = pan1 + 1000;
                int wobbleDown2 = turn1 + 2000;
                int turn2 = wobbleDown2 + 1700;
                int wobbleUp1 = turn2 + 1500;


                // organize commands into a list for easier calls below
                List<Integer> commands = new ArrayList<Integer>();
                commands.add(drive1);
                commands.add(wobbleDown1);
                commands.add(launch1);
                commands.add(reposition1);
                commands.add(launch2);
                commands.add(reposition2);
                commands.add(launch3);
                commands.add(drive2);
                commands.add(stop1);
                commands.add(pan1);
                commands.add(turn1);
                commands.add(wobbleDown2);
                commands.add(turn2);
                commands.add(wobbleUp1);

                // Inefficiency (tm)
                Integer[] c = commands.toArray(new Integer[0]); // convert list to array for indexing


                if (finalTime > 0 && finalTime <= c[0]) // drive forward to position for launching
                {
                    drive(D_FORWARD);

                    if (finalTime <= c[1])
                    {
                        wobble(W_DOWN);
                    }
                    else
                    {
                        wobble(W_STOP);
                    }
                }
                if (finalTime > c[0] && finalTime <= c[2]) // first launch
                {
                    drive(D_STOP);
                    toggleServo.setPower(0.5);
                }
                if (finalTime > c[2] && finalTime <= c[3]) // reposition
                {
                    pan(P_LEFT);
                    toggleServo.setPower(-0.5);
                }
                if (finalTime > c[3] && finalTime <= c[4]) // second launch
                {
                    pan(P_STOP);
                    toggleServo.setPower(0.5);
                }
                if (finalTime > c[4] && finalTime <= c[5]) // reposition
                {
                    pan(P_LEFT);
                    toggleServo.setPower(-0.5);
                }
                if (finalTime > c[5] && finalTime <= c[6]) // third launch
                {
                    pan(P_STOP);
                    toggleServo.setPower(0.5);
                }
                if (finalTime > c[6] && finalTime <= c[7]) // reset & move up to line
                {
                    drive(D_FORWARD);
                    toggleServo.setPower(-0.5);
                }
                if (finalTime > c[7] && finalTime <= c[8])
                {
                    drive(D_STOP); // stop on line
                }
                if (finalTime > c[8] && finalTime <= c[9])
                {
                   pan(P_LEFT);
                }
                if (finalTime > c[9] && finalTime <= c[10])
                {
                    pan(P_STOP);
                    turn(T_RIGHT);
                }
                if (finalTime > c[10] && finalTime <= c[11])
                {
                    turn(T_STOP);
                    wobble(W_DOWN);
                }
                if (finalTime > c[11] && finalTime <= c[12])
                {
                    wobble(W_STOP);
                    turn(T_RIGHT);
                }
                if (finalTime > c[12] && finalTime <= c[13])
                {
                    turn(T_STOP);
                    wobble(W_UP);
                }
                if (finalTime > c[13])
                {
                    wobble(W_STOP); // final stop
                }
            }
        }
    }
}

//0.6 is the speed to hit powershot