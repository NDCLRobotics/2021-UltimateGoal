package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.util.Range;
import java.lang.StrictMath;


@TeleOp(name="SportTele", group="Interactive Opmode")

public class yeetV2ElectricBoogaloo extends OpMode
{
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

    // boolean for starting the auto- ring lift/deposit
    private boolean autoGrabbing = false;
    private boolean autoFlicking = false;

    // scale down power from max for driving
    private double powerScale = 0.75;
    private double launchPowerScale = 0.5;
    private double highLaunchPowerScale = 0.8;

    // doubles for driving and panning
    private double frontLeftPower, frontRightPower, backLeftPower, backRightPower;
    private double frontLeftPan, frontRightPan, backLeftPan, backRightPan;

    // variables for wobble motor
    private double wobbleSpeed;

    // frame counter
    private long currentFrame;
    private long startGrabFrame;
    private long startFlickFrame;

    // temporary variable for testing
    private double tempTestingVariable = 0.0;


    // function to do the following from a single button press:
    //  grab with ring claw
    //  raise ring claw
    //  release ring claw
    //  lower ring claw
    private void autoGrab()
    {
        long grabTime = 230; // how long it takes to grab
        long raiseTime = grabTime + 150; // how long it takes to raise
        long releaseTime = raiseTime + 50; // how long it takes to release & Lower
        long lowerTime = releaseTime + 100; // how long it takes to lower


        if (currentFrame > startGrabFrame && currentFrame <= startGrabFrame + grabTime) // grabbing
        {
            ringServo.setPower(0.12);
        }
        if (currentFrame > startGrabFrame + grabTime && currentFrame <= startGrabFrame + raiseTime) // raising
        {
            flipServo.setPower(-0.80);
        }
        if (currentFrame > startGrabFrame + raiseTime && currentFrame <= startGrabFrame + releaseTime) // release
        {
            ringServo.setPower(-0.06);
        }
        if (currentFrame > startGrabFrame + releaseTime && currentFrame <= startGrabFrame + lowerTime) // lower
        {
            flipServo.setPower(0.85);
        }
        if (currentFrame >  startGrabFrame + lowerTime)
        {
            // stop
            autoGrabbing = false;
        }
    }

    private void autoFlick()
    {
        long pushTime = 50; // how long it takes to push
        telemetry.addData("say:", "AutoFlick");

        if (currentFrame > startFlickFrame && currentFrame <= startFlickFrame + pushTime) // pushing
        {
            toggleServo.setPower(0.5);
            telemetry.addData("say:", "AutoFlick Forward");
        }
        if (currentFrame >  startFlickFrame + pushTime) // retracting & stopping task
        {
            toggleServo.setPower(-0.5);
            autoFlicking = false;
            telemetry.addData("say:", "Ending AutoFlick");
        }
    }



    @Override
    public void init () {
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
        backLeftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        backRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        launchingMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        wobbleMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        // message displayed on the phone before initialization
        telemetry.addData("say: ", "Working, latest updated: never lol");

        // initial claw position
        ringServo.setPower(0.00);
        currentFrame = 0;
        startGrabFrame = 0;
        startFlickFrame = 0;
    }

    @Override
    public void init_loop() {

    }

    @Override
    public void loop() {

        // messages displayed on the phone while running
        telemetry.addData("say:", "Working. Last Updated: never lol ecks dee");
        telemetry.addData("say:", "Power Scale equals: " + powerScale);
        telemetry.addData("say:", "High Launcher Speed equals: " + highLaunchPowerScale);
        telemetry.addData("say:", "Wobble motor current position: " + wobbleMotor.getCurrentPosition());
        telemetry.addData("say:", "Current frame: " + currentFrame);
        telemetry.addData("say:", "Start frame: " + startGrabFrame);


        // increment the frame counter
        currentFrame += 1;


        // increase or decrease powerScale
        if (gamepad1.dpad_up && !powerSwitching)
        {
            powerSwitching = true;
            powerScale += 0.25;
        }
        else if (gamepad1.dpad_down && !powerSwitching)
        {
            powerSwitching = true;
            powerScale -= 0.25;
        }
        else if (!gamepad1.dpad_down && !gamepad1.dpad_up && powerSwitching)
        {
            powerSwitching = false;
        }

        // clamp powerScale and panPower to [0.25, 1.0]
        if (powerScale > 1.0)
        {
            powerScale = 1.0;
        }
        else if (powerScale < 0.25)
        {
            powerScale = 0.25;
        }

        // linking the drive commands to the controller
        double drive = -gamepad1.left_stick_y;
        double turn = gamepad1.right_stick_x;
        double pan = -gamepad1.left_stick_x;

        // panning, capped at -1 and 1
        if ((gamepad1.dpad_left || gamepad1.dpad_right) || StrictMath.abs(gamepad1.left_stick_x) > StrictMath.abs(gamepad1.right_stick_x)) {
            frontLeftPan = Range.clip(drive - pan, -1.0, 1.0);
            frontRightPan = Range.clip(drive + pan, -1.0, 1.0);
            backLeftPan = Range.clip(-drive - pan, -1.0, 1.0);
            backRightPan = Range.clip(-drive + pan, -1.0, 1.0);
            frontLeftMotor.setPower(powerScale * frontLeftPan);
            frontRightMotor.setPower(powerScale * frontRightPan);
            backLeftMotor.setPower(powerScale * backLeftPan);
            backRightMotor.setPower(powerScale * backRightPan);
        }
         // turning, capped at -1 and 1
        else {
            frontLeftPower = Range.clip(drive + turn, -1.0, 1.0);
            frontRightPower = Range.clip(drive - turn, -1.0, 1.0);
            backLeftPower = Range.clip(-drive - turn, -1.0, 1.0);
            backRightPower = Range.clip(-drive + turn, -1.0, 1.0);
            frontLeftMotor.setPower(powerScale * frontLeftPower);
            frontRightMotor.setPower(powerScale * frontRightPower);
            backLeftMotor.setPower(powerScale * backLeftPower);
            backRightMotor.setPower(powerScale * backRightPower);
        }

        // launching motor
        // set launcher power to double if right trigger is being held
        if (gamepad1.right_trigger > 0)
        {
            launchPowerScale = highLaunchPowerScale;
        }
        else
        {
            launchPowerScale = 0.5;
        }
        if (gamepad1.dpad_left && !launchSwitching)
        {
            highLaunchPowerScale -= 0.2;
            launchSwitching = true;
        }
        else if (gamepad1.dpad_right && !launchSwitching)
        {
            highLaunchPowerScale += 0.2;
            launchSwitching = true;
        }
        else if (!gamepad1.dpad_left && !gamepad1.dpad_right && launchSwitching)
        {
            launchSwitching = false;
        }
        // clamp launchScale to [0.1,1.0]
        if (highLaunchPowerScale > 1.0)
        {
            highLaunchPowerScale = 1.0;
        }
        else if (highLaunchPowerScale < -0.2)
        {
            highLaunchPowerScale = -0.2;
        }

        launchingMotor.setPower(launchPowerScale);

        /*
        // toggleServo
        if (gamepad1.left_trigger > 0 && !toggleSwitching)
            toggleBool = !toggleBool;
            toggleSwitching = true;
        if (gamepad1.left_trigger <= 0 && toggleSwitching)
            toggleSwitching = false;

        if (toggleBool)
            toggleServo.setPower(0.5);
        else
            toggleServo.setPower(-0.5);
        */

        // flipServo and ringServo
        if (gamepad1.b)
        {
            if (gamepad1.left_bumper) // lifted claw
            {
                flipServo.setPower(-0.80);
                telemetry.addData("say:", "A");
            }
            if (gamepad1.right_bumper) // lowered claw
            {
                flipServo.setPower(0.85);
                telemetry.addData("say:", "B");
            }
        }
        if (!gamepad1.b)
        {
            if (gamepad1.left_bumper) // opened claw?
            {
                ringServo.setPower(-0.06);
                telemetry.addData("say:", "C");
            }
            if (gamepad1.right_bumper) // closed claw?
            {
                ringServo.setPower(0.12);
                telemetry.addData("say:", "D");
            }
        }

        // wobble motor
        // limits are currently hard-coded: removing the wobble motor manually will mess with this


        if (gamepad1.start && !gamepad1.back)// && wobbleMotor.getCurrentPosition() > -3100) // go up
        {
            wobbleMotor.setPower(-0.55);
        }
        /*else if (wobbleMotor.getCurrentPosition() <= -3100) // limit (carrying)
        {
            wobbleMotor.setPower(0.0);
        }*/

        if (gamepad1.back && !gamepad1.start)// && wobbleMotor.getCurrentPosition() < 100) // go down
        {
            wobbleMotor.setPower(0.55);
        }
        /*else if (wobbleMotor.getCurrentPosition() >= 100) // limit (horizontal)
        {
            wobbleMotor.setPower(0.0);
        }*/

        if (!gamepad1.back && !gamepad1.start)
        {
            wobbleMotor.setPower(0);
        }



        if (gamepad1.a || autoGrabbing) // start auto grab function
        {

            if (!autoGrabbing)
            {
                startGrabFrame = currentFrame;
            }

            autoGrabbing = true;
            autoGrab();
        }

        if (gamepad1.x) // cancel auto grab function
        {
            autoGrabbing = false;
        }


        if (gamepad1.left_trigger > 0 || autoFlicking)
        {
            if (!autoFlicking)
            {
               startFlickFrame = currentFrame;
            }

            autoFlicking = true;
            autoFlick();
        }

    }

        /*
        if (gamepad1.start && wobbleMotor.getCurrentPosition() < 65)
            wobbleMotor.setPower(0.75);
        if (gamepad1.back && wobbleMotor.getCurrentPosition() > -15)
            wobbleMotor.setPower(-0.75);
        if ((!gamepad1.start && !gamepad1.back) || wobbleMotor.getCurrentPosition() <= -15 || wobbleMotor.getCurrentPosition() >= 65)
            wobbleMotor.setPower(0.0);
        */

        //wobbleMotor.setPower(wobbleSpeed);  //yeet

    @Override
    public void stop()
    {
        // pls do, kind sir
        frontLeftMotor.setPower(0);
        frontRightMotor.setPower(0);
        backLeftMotor.setPower(0);
        backRightMotor.setPower(0);
        launchingMotor.setPower(0);
        wobbleMotor.setPower(0);
    }

}