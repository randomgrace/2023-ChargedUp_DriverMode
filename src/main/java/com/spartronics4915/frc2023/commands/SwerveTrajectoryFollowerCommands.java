package com.spartronics4915.frc2023.commands;

import java.util.ArrayList;
import static com.spartronics4915.frc2023.Constants.Trajectory.*;
import static com.spartronics4915.frc2023.Constants.Swerve.*;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;

import com.spartronics4915.frc2023.subsystems.Swerve;

public class SwerveTrajectoryFollowerCommands {
	private final Swerve mSwerve;
	private final PIDController mXPID, mYPID;
	private final ProfiledPIDController mThetaPID;

	public SwerveTrajectoryFollowerCommands() {
		mSwerve = Swerve.getInstance();
		mXPID = new PIDController(kLinearP, 0, 0);
		mYPID = new PIDController(kLinearP, 0, 0);
		mThetaPID = new ProfiledPIDController(
			kThetaP, 0, 0, new TrapezoidProfile.Constraints(kMaxAngularSpeed, 1) // FIXME: 1 is a placeholder
		);
		mThetaPID.enableContinuousInput(-Math.PI, Math.PI);
	}

	class FollowTrajectory extends SwerveControllerCommand {
		private TrapezoidProfile.Constraints mConstraints;
		public FollowTrajectory(
			ArrayList<Pose2d> waypoints, // meters
			double startVelocity, double endVelocity, double maxVelocity, // meters per second
			double maxAccel, // meters per second squared
			double maxAngularVelocity, double maxAngularAcceleration // radians per second
		) {
			super(
				TrajectoryGenerator.generateTrajectory( // FIXME: will possibly take longer than 1 cycle (don't worry 'bout it)
					waypoints,
					new TrajectoryConfig(maxVelocity, maxAccel)
						.setStartVelocity(startVelocity)
						.setEndVelocity(endVelocity)
				),
				mSwerve::getPose,
				kKinematics,
				mXPID, mYPID,
				mThetaPID,
				mSwerve::setModuleStates,
				mSwerve
			);
			mConstraints = new TrapezoidProfile.Constraints(maxAngularVelocity, maxAngularAcceleration);
		}

		@Override
		public void initialize() {
			mThetaPID.setConstraints(mConstraints);
			mThetaPID.reset(mSwerve.getYaw().getRadians());
			super.initialize();
		}

		@Override
		public void execute() {
			super.execute();
			SmartDashboard.putNumber("Swerve xPID Setpoint", mXPID.getSetpoint());
			SmartDashboard.putNumber("Swerve xPID Position Error", mXPID.getPositionError());
			
			SmartDashboard.putNumber("Swerve yPID Setpoint", mYPID.getSetpoint());
			SmartDashboard.putNumber("Swerve yPID Position Error", mYPID.getPositionError());
			
			SmartDashboard.putNumber("Swerve thetaPID Setpoint", mThetaPID.getSetpoint().velocity);
			SmartDashboard.putNumber("Swerve thetaPID Position Error", mThetaPID.getPositionError());
		}
	}
}