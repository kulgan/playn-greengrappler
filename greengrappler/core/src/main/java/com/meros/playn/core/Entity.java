package com.meros.playn.core;

import java.util.EnumSet;

import playn.core.Canvas;

import com.meros.playn.core.Constants.Direction;

public abstract class Entity {
	
	protected int mFrameCounter = 0;
	protected float2 mPosition = new float2();
	protected boolean mRemoved = false;
	protected Room mRoom = null;

	protected float2 mSize = new float2();

	
	public class CollisionRect {
		public float2 myBottomRight;
		public float2 myTopLeft;
	}

	protected static boolean Collides(CollisionRect aRect1, CollisionRect aRect2) {
		if (aRect1.myBottomRight.x <= aRect2.myTopLeft.x)
			return false;

		if (aRect1.myTopLeft.x >= aRect2.myBottomRight.x)
			return false;

		if (aRect1.myBottomRight.y <= aRect2.myTopLeft.y)
			return false;

		if (aRect1.myTopLeft.y >= aRect2.myBottomRight.y)
			return false;

		return true;
	}

	protected float2 mVelocity = new float2();

	// virtual void draw(BITMAP *buffer, int offsetX, int offsetY, int layer);
	public void draw(Canvas aBuffer, int offsetX, int offsetY, int layer) {
		int x = getDrawPositionX() + offsetX;
		int y = getDrawPositionY() + offsetY;
		int x1 = (int) (x - getHalfSize().x);
		int y1 = (int) (y - getHalfSize().y);
		int x2 = (int) (x + getHalfSize().x);
		int y2 = (int) (y + getHalfSize().y);
		// buffer.strokeRect(x1, y1, x2-x1, y2-y1);
		// buffer.drawLine(x - 3, y, x + 3, y);
		// buffer.drawLine(x, y - 3, x, y + 3);
	}

	//
	// virtual CollisionRect getCollisionRect();
	public CollisionRect getCollisionRect() {
		CollisionRect collisionRect = new CollisionRect();
		collisionRect.myTopLeft = getPosition().subtract(getHalfSize());
		collisionRect.myBottomRight = getPosition().add(getHalfSize());

		return collisionRect;
	}

	// virtual int getDrawPositionX();
	public int getDrawPositionX() {
		return (int) (getPosition().x + 0.5);
	}

	// virtual int getDrawPositionY();
	public int getDrawPositionY() {
		return (int) (getPosition().y + 0.5);
	}

	// virtual float2 getHalfSize();
	public float2 getHalfSize() {
		return mSize.multiply(0.5f);
	}

	// virtual int getLayer() = 0;
	public abstract int getLayer();

	// virtual float2 getPosition();
	public float2 getPosition() {
		return (float2) mPosition.clone();
	}

	// virtual Room *getRoom();
	public Room getRoom() {
		return mRoom;
	}

	// virtual float2 getSize();
	public float2 getSize() {
		return (float2) mSize.clone();
	}

	// virtual float2 getVelocity();
	public float2 getVelocity() {
		return (float2) mVelocity.clone();
	}

	// virtual bool isDamagable();
	public boolean isDamagable() {
		return false;
	}

	// virtual bool isHookable();
	public boolean isHookable() {
		return false;
	}

	// virtual bool isRemoved();
	public boolean isRemoved() {
		return mRemoved;
	}

	// virtual unsigned int moveWithCollision();
	public EnumSet<Direction> moveWithCollision() {
		return moveWithCollision(mVelocity.divide(Time.TicksPerSecond));
	}

	public EnumSet<Direction> moveWithCollision(float2 delta) {
		// FIXME: implement
		int substeps = (int) Math
				.ceil((Math.abs(delta.x) + Math.abs(delta.y)) * 0.2);
		delta = delta.divide(substeps);
		EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
		float2 halfSize = getHalfSize();

		for (int i = 0; i < substeps; i++) {
			mPosition.x += delta.x;
			int x1 = (int) ((mPosition.x - halfSize.x) / mRoom.getTileWidth());
			int x2 = (int) ((mPosition.x + halfSize.x) / mRoom.getTileWidth());
			int y1n = (int) ((mPosition.y - halfSize.y + 0.01f) / mRoom
					.getTileHeight());
			int y2n = (int) ((mPosition.y + halfSize.y - 0.01f) / mRoom
					.getTileHeight());

			if (delta.x > 0) {
				for (int y = y1n; y <= y2n; y++) {
					if (mRoom.isCollidable(x2, y)) {
						delta.x = 0;
						result.add(Direction.RIGHT);
						mPosition.x = x2 * mRoom.getTileWidth() - halfSize.x;
						break;
					}
				}
			} else if (delta.x < 0) {
				for (int y = y1n; y <= y2n; y++) {
					if (mRoom.isCollidable(x1, y)) {
						delta.x = 0;
						result.add(Direction.LEFT);
						mPosition.x = (x1 + 1) * mRoom.getTileWidth()
								+ halfSize.x;
						break;
					}
				}
			}

			mPosition.y += delta.y;
			int y1 = (int) ((mPosition.y - halfSize.y) / mRoom.getTileHeight());
			int y2 = (int) ((mPosition.y + halfSize.y) / mRoom.getTileHeight());
			int x1n = (int) ((mPosition.x - halfSize.x + 0.01f) / mRoom
					.getTileWidth());
			int x2n = (int) ((mPosition.x + halfSize.x - 0.01f) / mRoom
					.getTileWidth());

			if (delta.y > 0) {
				for (int x = x1n; x <= x2n; x++) {
					if (mRoom.isCollidable(x, y2)) {
						delta.y = 0;
						result.add(Direction.DOWN);
						mPosition.y = y2 * mRoom.getTileHeight() - halfSize.y;
						break;
					}
				}
			} else if (delta.y < 0) {
				for (int x = x1n; x <= x2n; x++) {
					if (mRoom.isCollidable(x, y1)) {
						delta.y = 0;
						result.add(Direction.UP);
						mPosition.y = (y1 + 1) * mRoom.getTileHeight()
								+ halfSize.y;
					}
				}
			}
		}

		return result;
	}

	// virtual void onBossFloorActivate();
	public void onBossFloorActivate() {

	}

	// virtual void onBossWallActivate();
	public void onBossWallActivate() {

	}

	// virtual void onBossWallDeactivate();
	public void onBossWallDeactivate() {

	}

	// virtual void onButtonDown(int aId);
	public void onButtonDown(int aId) {

	}

	// virtual void onButtonUp(int aId);
	public void onButtonUp(int aId) {

	}

	// virtual void onDamage();
	public void onDamage() {

	}

	// virtual void onLevelComplete();
	public void onLevelComplete() {

	}

	// virtual void onRespawn();
	public void onRespawn() {

	}

	// virtual void onStartWallOfDeath();
	public void onStartWallOfDeath() {

	}

	//
	// virtual void remove();
	public void remove() {
		mRemoved = true;
	}

	// virtual void setPosition(float2 position);
	public void setPosition(float2 position) {
		mPosition = (float2) position.clone();
	}

	//
	// virtual void setRoom(Room *room);
	public void setRoom(Room room) {
		mRoom = room;
	}

	// virtual void setSize(float2 size);
	public void setSize(float2 size) {
		mSize = (float2) size.clone();
	}

	// virtual void setVelocity(float2 velocity);
	public void setVelocity(float2 velocity) {
		mVelocity = (float2) velocity.clone();
	}

	// virtual void update();
	public void update() {
		mFrameCounter++;
	}

}
