package se.darkbits.greengrappler.entities;

import java.util.EnumSet;

import playn.core.Surface;
import se.darkbits.greengrappler.Entity;
import se.darkbits.greengrappler.PlayerSkill;
import se.darkbits.greengrappler.Resource;
import se.darkbits.greengrappler.Constants.Direction;
import se.darkbits.greengrappler.floatpair.ImmutableFloatPair;
import se.darkbits.greengrappler.media.Animation;
import se.darkbits.greengrappler.media.Sound;


public class SimpleWalkingMonster extends Entity {

	enum Facing {
		LEFT, RIGHT
	}

	enum State {
		IDLING, WALKING
	}

	float IDLE_TO_WALK_CHANCE = 0.05f;

	Animation myAnimation = Resource.getAnimation("data/images/robot.bmp", 4);

	Facing myFacing = Facing.LEFT;

	int myFrame = 0;

	State myState = State.IDLING;

	float WALK_TO_IDLE_CHANCE = 0.01f;

	float WALKING_SPEED = 14.0f;

	public SimpleWalkingMonster() {
		setSize(new ImmutableFloatPair(20, 20));
	}

	public void die() {
		Sound.playSample("data/sounds/damage");

		ParticleSystem ps = new ParticleSystem(Resource.getAnimation(
				"data/images/debris.bmp", 4), 10, 30, 10, 1, 50, 5,
				new ImmutableFloatPair(0.0f, -30.0f), 2.0f);
		ps.setPosition(getPosition(), 5.0f, false);
		myRoom.addEntity(ps);
		remove();
	}

	@Override
	public void draw(Surface buffer, int offsetX, int offsetY, int layer) {
		float x = getPosition().getX() - myAnimation.getFrameWidth() / 2
				+ offsetX;
		float y = getPosition().getY() - myAnimation.getFrameHeight() / 2
				+ offsetY;

		myAnimation.drawFrame(buffer, myFrame / 15, (int) x, (int) y,
				myFacing == Facing.RIGHT, false);
	}

	@Override
	public int getLayer() {
		return 1;
	}

	@Override
	public boolean isDamagable() {
		return true;
	}

	@Override
	public void onDamage() {
		PlayerSkill.playerDidSomethingClever(0.5f, 0.1f);
		die();
	}

	@Override
	public void update() {
		switch (myState) {
		case WALKING: {

			myVelocity.set(20.0f * ((myFacing == Facing.LEFT) ? -1 : 1),
					myVelocity.getY() + 6.0f);

			setVelocity(myVelocity);
			EnumSet<Direction> bumps = moveWithCollision();

			if (bumps.contains(Direction.UP) || bumps.contains(Direction.DOWN)) {
				myVelocity.set(myVelocity.getX(), 0);
			}

			if (bumps.contains(Direction.LEFT)) {
				myFacing = Facing.RIGHT;
			}

			if (bumps.contains(Direction.RIGHT)) {
				myFacing = Facing.LEFT;
			}

			int offsetX = (int) ((myFacing == Facing.RIGHT) ? -getHalfSize()
					.getX() - 2 : getHalfSize().getX() + 2);
			int offsetY = (int) (getHalfSize().getY() + 2);

			int x = (int) ((getPosition().getX() + offsetX) / myRoom
					.getTileWidth());
			int y = (int) ((getPosition().getY() + offsetY) / myRoom
					.getTileHeight());

			if (!myRoom.isCollidable(x, y)) {
				if (myFacing == Facing.LEFT) {
					myFacing = Facing.RIGHT;
				} else {
					myFacing = Facing.LEFT;
				}
			}

			if (Math.random() < WALK_TO_IDLE_CHANCE) {
				myState = State.IDLING;
			}
		}

			break;
		case IDLING:

			if (Math.random() < IDLE_TO_WALK_CHANCE) {
				if (Math.random() > 0.5) {
					myFacing = Facing.LEFT;
				} else {
					myFacing = Facing.RIGHT;
				}
				myState = State.WALKING;
			}

			break;
		}

		Hero hero = myRoom.getHero();
		if (hero.Collides(this)) {
			hero.kill();
		}

		myFrame++;
	}
}
