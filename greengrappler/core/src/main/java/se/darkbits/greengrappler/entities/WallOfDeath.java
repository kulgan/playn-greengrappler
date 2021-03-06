package se.darkbits.greengrappler.entities;

import playn.core.Surface;
import se.darkbits.greengrappler.Entity;
import se.darkbits.greengrappler.PlayerSkill;
import se.darkbits.greengrappler.Resource;
import se.darkbits.greengrappler.UtilMethods;
import se.darkbits.greengrappler.floatpair.ImmutableFloatPair;
import se.darkbits.greengrappler.media.Animation;
import se.darkbits.greengrappler.media.Sound;


public class WallOfDeath extends Entity {

	private boolean myInited = false;
	private boolean myBoost = false;
	private float mySpeed = 0.0f;
	private int myFrameCounter = 0;
	private boolean myRunning = false;
	private float mySoundCountdown = 10.0f;
	private Animation mySaw = Resource.getAnimation("data/images/saw.bmp");
	private ImmutableFloatPair myOriginalPosition;

	public WallOfDeath() {
		setSize(new ImmutableFloatPair(10, 12 * 100));
	}

	@Override
	public void draw(Surface buffer, int offsetX, int offsetY, int layer) {
		int x = (int) (getDrawPositionX() + offsetX - getSize().getX() / 2);
		int y = (int) (getDrawPositionY() + offsetY - getSize().getY() / 2);

		int saws = (int) getSize().getY() / 12;

		for (int i = 0; i < saws; i++) {
			mySaw.drawFrame(buffer, myFrameCounter / 5 + i, x, y + i * 12);
		}
		// Entity::draw(buffer, offsetX, offsetY, layer);
	}

	@Override
	public void update() {
		myFrameCounter++;

		if (!myInited) {
			myOriginalPosition = new ImmutableFloatPair(getPosition());
			myInited = true;
		}

		if (!myRunning)
			return;

		if (getPosition().getX() > 2950.0f) {
			ParticleSystem ps = new ParticleSystem(mySaw, 2, 100, 20, 10, 20,
					1, new ImmutableFloatPair(0.0f, -50.0f), 4.0f);

			int x = (int) (getDrawPositionX() - getSize().getX() / 2);
			int y = (int) (getDrawPositionY() - getSize().getY() / 2);
			int saws = (int) getSize().getY() / 12;

			for (int i = 0; i < saws; i++) {
				ps.setPosition(new ImmutableFloatPair(x, y + i * 12), 0.1f,
						true);
			}

			myRoom.addEntity(ps);
			remove();
			return;
		}

		for (int xo = 0; xo < 3; xo++) {
			int xt = xo
					+ (int) ((getPosition().getX() - getSize().getX() / 2) / myRoom
							.getTileWidth());
			myRoom.destroyToTileRow(xt - 1);
		}

		float heroX = myRoom.getHero().getPosition().getX();
		float x = getPosition().getX();

		float distance = heroX - x;
		if (distance > 190 && !myBoost) {
			setPosition(new ImmutableFloatPair(heroX - 190, getPosition()
					.getY()));
		}

		if (distance < 80) {
			myBoost = false;
		}

		if (myBoost) {
			mySpeed = (int) UtilMethods.lerp(5.0f, 9.0f, PlayerSkill.get());
		} else {
			float wantedSpeed = UtilMethods.lerp(1.0f, 1.9f, PlayerSkill.get());
			mySpeed = UtilMethods.lerp(mySpeed, wantedSpeed, 0.05f);
		}

		mySoundCountdown -= mySpeed;
		if (mySoundCountdown <= 0.0f) {
			mySoundCountdown += 10.0f;
			Sound.playSample("data/sounds/damage");
		}

		setPosition(new ImmutableFloatPair(getPosition().getX() + mySpeed,
				getPosition().getY()));

		if (myRoom.getHero().Collides(this)) {
			myRoom.getHero().kill();
		}
	}

	@Override
	public void onRespawn() {
		myRoom.destroyToTileRow(-1);
		setPosition(myOriginalPosition);
		myRunning = false;
		myBoost = false;
		mySoundCountdown = 10.0f;
	}

	@Override
	public void onStartWallOfDeath() {
		if (!myRunning) {
			myRunning = true;
		} else {
			myBoost = true;
		}
	}

	@Override
	public int getLayer() {
		return 1;
	}

}
