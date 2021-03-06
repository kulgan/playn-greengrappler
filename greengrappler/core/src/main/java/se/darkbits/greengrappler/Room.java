package se.darkbits.greengrappler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import playn.core.Color;
import playn.core.Surface;
import se.darkbits.greengrappler.entities.Coin;
import se.darkbits.greengrappler.entities.Hero;
import se.darkbits.greengrappler.entities.ParticleSystem;
import se.darkbits.greengrappler.floatpair.AbstractFloatPair;
import se.darkbits.greengrappler.floatpair.FloatPair;
import se.darkbits.greengrappler.floatpair.ImmutableFloatPair;
import se.darkbits.greengrappler.media.Font;
import se.darkbits.greengrappler.media.Music;


public class Room {
	private final Camera mCamera;
	ArrayList<Entity> mEntities = new ArrayList<Entity>();

	ArrayList<Entity> mDamagableEntities = new ArrayList<Entity>();
	ArrayList<Entity> mHookableEntities = new ArrayList<Entity>();
	ArrayList<Entity> mEntitiesToAdd = new ArrayList<Entity>();
	Hero mHero = null;

	Layer myBackgroundLayer;

	Font myFont = Resource.getFont("data/images/font.bmp");
	Layer myForegroundLayer;
	boolean myHookableArray[][];
	boolean myCollidableArray[][];

	int myFrameCounter = 0;

	boolean myHeroIsDead = false;

	boolean myIsCompleted = false;
	Layer myMiddleLayer;
	private int myDestroyedToTileRow = -1;
	private FloatPair findHookableEntityToPos = new FloatPair();
	private FloatPair myRayCastLengthCompareFloatPair = new FloatPair();
	private SortedMap<Integer, SortedMap<Integer, Coin>> mSortedCoins = new TreeMap<Integer, SortedMap<Integer, Coin>>();
	private Layer[] myBgLayers;
	private Layer[] myMidLayers;
	private Layer[] myFgLayers;
	private Comparator<Entity> myDrawOrderComperator = new Comparator<Entity>() {
		@Override
		public int compare(Entity aEnt1, Entity aEnt2) {
			if (aEnt1.getLayer() != aEnt2.getLayer())
				return aEnt1.getLayer() - aEnt2.getLayer();

			return aEnt1.getClass().getName()
					.compareTo(aEnt2.getClass().getName());
		}
	};

	public Room(Layer aBackgroundLayer, Layer aMiddleLayer,
			Layer aForegroundLayer, Camera aCamera) {

		myBackgroundLayer = aBackgroundLayer;
		myMiddleLayer = aMiddleLayer;
		myForegroundLayer = aForegroundLayer;

		myBgLayers = new Layer[] { myMiddleLayer, myForegroundLayer };
		myMidLayers = new Layer[] { myForegroundLayer };
		myFgLayers = new Layer[] {};

		mCamera = aCamera;

		myHookableArray = new boolean[getWidthInTiles()][getHeightInTiles()];
		myCollidableArray = new boolean[getWidthInTiles()][getHeightInTiles()];

		for (int x = 0; x < getWidthInTiles(); x++) {
			for (int y = 0; y < getHeightInTiles(); y++) {
				myHookableArray[x][y] = myMiddleLayer.getTile(x, y).getHook();
				myCollidableArray[x][y] = myMiddleLayer.getTile(x, y)
						.getCollide();
			}
		}
	}

	public void addEntity(Entity aEntity) {
		if (aEntity instanceof Hero) {
			mHero = (Hero) aEntity;
		}

		aEntity.setRoom(this);
		mEntitiesToAdd.add(aEntity);
	}

	public void broadcastBoosFloorActivate() {
		for (int i = 0; i < mEntities.size(); i++) {
			Entity entity = mEntities.get(i);

			entity.onBossFloorActivate();
		}
	}

	public void broadcastBoosWallActivate() {
		for (int i = 0; i < mEntities.size(); i++) {
			Entity entity = mEntities.get(i);

			entity.onBossWallActivate();
		}
	}

	public void broadcastBoosWallDectivate() {
		for (int i = 0; i < mEntities.size(); i++) {
			Entity entity = mEntities.get(i);

			entity.onBossWallDeactivate();
		}
	}

	public void broadcastStartWallOfDeath() {
		for (Entity entity : mEntities)
			entity.onStartWallOfDeath();
	}

	public boolean damageDone(int aX, int aY) {
		for (Entity entity : mDamagableEntities) {
			if (Collision.Collides(entity, aX, aY, aX + 1, aY + 1)) {
				entity.onDamage();
				return true;
			}
		}

		return false;
	}

	public Entity findHookableEntity(ImmutableFloatPair position) {
		for (Entity entity : mHookableEntities) {
			findHookableEntityToPos.set(entity.getPosition())
					.subtract(position);
			if (Math.abs(findHookableEntityToPos.getX()) < entity.getHalfSize()
					.getX()
					&& Math.abs(findHookableEntityToPos.getY()) < entity
							.getHalfSize().getY()) {
				return entity;
			}
		}

		return null;
	}

	public Camera getCamera() {
		return mCamera;
	}

	public Collection<Entity> getDamagableEntities() {
		return mDamagableEntities;
	}

	public int getHeightInTiles() {
		return myMiddleLayer.getHeight();
	}

	public Hero getHero() {
		return mHero;
	}

	public Collection<Entity> getHookableEntities() {
		return mHookableEntities;
	}

	public float getTileHeight() {
		return 10.0f;
	}

	public float getTileWidth() {
		return 10.0f;
	}

	public int getWidthInTiles() {
		return myMiddleLayer.getWidth();
	}

	public boolean isCollidable(int aX, int aY) {
		if (aX < 0 || aX >= myCollidableArray.length || aY < 0
				|| aY >= myCollidableArray[aX].length)
			return false;

		if (aX < myDestroyedToTileRow)
			return false;

		return myCollidableArray[aX][aY];
	}

	public boolean isCompleted() {
		if (myIsCompleted && myFrameCounter > 60 * 4) {
			Music.stop();
			Input.enable();
			return true;
		}

		return false;
	}

	public boolean isHookable(int aX, int aY) {
		if (aX < 0 || aX >= myHookableArray.length || aY < 0
				|| aY >= myHookableArray[aX].length)
			return false;

		if (aX < myDestroyedToTileRow)
			return false;

		return myHookableArray[aX][aY];
	}

	public void onDraw(Surface aBuffer) {
		if (mHero.isDead()) {
			if (myFrameCounter == 0 || myFrameCounter == 5
					|| myFrameCounter == 10)
				aBuffer.setFillColor(Color.rgb(231, 215, 156));
			else
				aBuffer.setFillColor(Color.rgb(57, 56, 41));

			aBuffer.fillRect(0, 0, 320, 240);

			if (myFrameCounter < 60)
				mHero.draw(aBuffer, mCamera.getOffsetX(), mCamera.getOffsetY(),
						0);
			return;
		}

		myBackgroundLayer.draw(aBuffer, mCamera.getOffsetX(),
				mCamera.getOffsetY(), myBgLayers);
		myMiddleLayer.draw(aBuffer, mCamera.getOffsetX(), mCamera.getOffsetY(),
				myMidLayers);

		for (Entity entity : mEntities) {
			entity.draw(aBuffer, mCamera.getOffsetX(), mCamera.getOffsetY(), 0);
		}

		myForegroundLayer.draw(aBuffer, mCamera.getOffsetX(),
				mCamera.getOffsetY(), myFgLayers);

		if (myIsCompleted) {
			myFont.drawCenter(aBuffer, "LEVEL COMPLETED!", 0, 0, 320, 240);
		}
	}

	public void onLogic() {
		myFrameCounter++;

		if (mEntitiesToAdd.size() > 0) {
			for (Entity entity : mEntitiesToAdd) {
				if (entity.isDamagable())
					mDamagableEntities.add(entity);

				if (entity.isHookable())
					mHookableEntities.add(entity);

				if (entity instanceof Coin) {
					if (!((Coin) entity).isDynamic()) {
						SortedMap<Integer, Coin> yMap = mSortedCoins
								.get((int) entity.getPosition().getX());

						if (yMap == null) {
							yMap = new TreeMap<Integer, Coin>();
							mSortedCoins.put((int) entity.getPosition().getX(),
									yMap);
						}

						yMap.put((int) entity.getPosition().getY(),
								(Coin) entity);
					}
				}
			}
			mEntities.addAll(mEntitiesToAdd);

			// Sort to draw order
			Collections.sort(mEntities, myDrawOrderComperator);

			mEntitiesToAdd.clear();
		}

		if (myIsCompleted && myFrameCounter == 60)
			Music.playSong("data/music/level_completed.xm");

		if (mHero.isDead() && !myHeroIsDead) {
			myFrameCounter = 0;
			myHeroIsDead = true;
			return;
		}

		if (mHero.isDead() && myFrameCounter > 120) {
			mHero.respawn();

			Iterator<Entity> it = mEntities.iterator();
			while (it.hasNext()) {
				it.next().onRespawn();
			}

			getCamera().centerToHero(mHero);
			getCamera().onRespawn();
			myHeroIsDead = false;
		}

		if (mHero.isDead()) {
			return;
		}

		for (int i = 0; i < mEntities.size(); i++) {
			Entity entity = mEntities.get(i);
			if (entity.isRemoved()) {

				mEntities.remove(i);
				mHookableEntities.remove(entity);
				mDamagableEntities.remove(entity);

				i--;
			}
		}

		for (int i = 0; i < mEntities.size(); i++) {
			Entity entity = mEntities.get(i);

			if (mHero.isDead())
				break;

			if (entity != mHero)
				entity.update();
		}

		// Update hero
		mHero.update();

		// Coins
		Collection<SortedMap<Integer, Coin>> yMaps = mSortedCoins
				.subMap((int) (mHero.getPosition().getX()
						- mHero.getHalfSize().getX() - Coin.COIN_SIZE / 2),
						(int) (mHero.getPosition().getX()
								+ mHero.getHalfSize().getX() + Coin.COIN_SIZE / 2))
				.values();

		for (SortedMap<Integer, Coin> yMap : yMaps) {
			Collection<Coin> coins = yMap.subMap(
					(int) (mHero.getPosition().getY()
							- mHero.getHalfSize().getY() - Coin.COIN_SIZE / 2),
					(int) (mHero.getPosition().getY()
							+ mHero.getHalfSize().getY() + Coin.COIN_SIZE / 2))
					.values();

			for (Coin coin : coins) {
				if (coin.Collides(mHero)) {
					coin.setCollides();
				}
			}
		}

		for (int i = 0; i < mEntities.size(); i++) {
			Entity entity = mEntities.get(i);

			if (entity.getPosition().getX() - entity.getHalfSize().getX() > getWidthInTiles()
					* getTileWidth()
					|| entity.getPosition().getY()
							- entity.getHalfSize().getY() > getHeightInTiles()
							* getTileHeight()
					|| entity.getPosition().getX()
							+ entity.getHalfSize().getX() < 0
					|| entity.getPosition().getY()
							+ entity.getHalfSize().getY() < 0) {
				entity.remove();
			}

		}

		getCamera().update(getHero());
	}

	public class OutInt {
		public int myInt;
	}

	public boolean rayCast(AbstractFloatPair origin,
			AbstractFloatPair direction, boolean cullBeyondDirection,
			OutInt aOutX, OutInt aOutY) {
		if (direction.isZero()) {
			return false;
		}

		int ix = (int) ((int) Math.floor(origin.getX()) / getTileWidth());
		int iy = (int) ((int) Math.floor(origin.getY()) / getTileHeight());
		float dx = (origin.getX() / getTileWidth() - ix);
		float dy = (origin.getY() / getTileHeight() - iy);
		float tx = direction.getX() > 0 ? 1 : 0;
		float ty = direction.getY() > 0 ? 1 : 0;

		int minX = 0;
		int maxX = getWidthInTiles();
		int minY = 0;
		int maxY = getHeightInTiles();
		if (cullBeyondDirection) {
			minX = Math.max(minX,
					ix
							+ (int) (Math.min(0.0f, direction.getX())
									/ getTileWidth() - 1.0f));
			maxX = Math.min(maxX,
					ix
							+ (int) (Math.max(0.0f, direction.getX())
									/ getTileWidth() + 2.0f));
			minY = Math.max(minY,
					iy
							+ (int) (Math.min(0.0f, direction.getY())
									/ getTileHeight() - 1.0f));
			maxY = Math.min(maxY,
					iy
							+ (int) (Math.max(0.0f, direction.getY())
									/ getTileHeight() + 2.0f));
		}
		boolean hit = false;
		while (ix >= minX && ix < maxX && iy >= minY && iy < maxY) {
			if (isCollidable(ix, iy)) {
				hit = true;
				break;
			}

			if (direction.getY() == 0
					|| (direction.getX() != 0 && ((tx - dx) / direction.getX() < (ty - dy)
							/ direction.getY()))) {
				dy += direction.getY() * (tx - dx) / direction.getX();
				if (direction.getX() > 0) {
					ix++;
					dx = 0;
				} else {
					ix--;
					dx = 1;
				}
			} else {
				dx += direction.getX() * (ty - dy) / direction.getY();
				if (direction.getY() > 0) {
					iy++;
					dy = 0;
				} else {
					iy--;
					dy = 1;
				}
			}
		}
		if (hit) {
			aOutX.myInt = ix;
			aOutY.myInt = iy;
			ImmutableFloatPair tileCenter = new ImmutableFloatPair(ix
					* getTileWidth() + getTileWidth() / 2, iy * getTileHeight()
					+ getTileHeight() / 2);
			myRayCastLengthCompareFloatPair.set(origin).subtract(tileCenter);
			if (cullBeyondDirection
					&& myRayCastLengthCompareFloatPair.lengthCompare(direction) > 0) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	public void setCollidable(int aX, int aY, boolean aCollide) {
		if (aX < 0 || aX >= myCollidableArray.length || aY < 0
				|| aY >= myCollidableArray[aX].length)
			return;

		myCollidableArray[aX][aY] = aCollide;
	}

	public void setCompleted() {
		for (int i = 0; i < mEntities.size(); i++) {
			Entity entity = mEntities.get(i);

			entity.onLevelComplete();
		}

		myFrameCounter = 0;
		myIsCompleted = true;
		mHero.imortal();
		Input.disable();
		Music.stop();
	}

	public void setHookable(int aX, int aY, boolean aHook) {
		if (aX < 0 || aX >= myHookableArray.length || aY < 0
				|| aY >= myHookableArray[aX].length)
			return;

		myHookableArray[aX][aY] = aHook;
	}

	public void broadcastButtonUp(int aId) {
		for (Entity entity : mEntities) {
			entity.onButtonUp(aId);
		}
	}

	public void broadcastButtonDown(int aId) {
		for (Entity entity : mEntities) {
			entity.onButtonDown(aId);
		}
	}

	public void destroyToTileRow(int aX) {
		myDestroyedToTileRow = aX;

		for (int y = 0; y < myMiddleLayer.getHeight(); y++) {
			boolean spawnDebris = myMiddleLayer.getTile(aX, y).getCollide();
			if (spawnDebris) {
				ParticleSystem ps = new ParticleSystem(Resource.getAnimation(
						"data/images/debris.bmp", 4), 20, 30, 10, 15, 40, 3,
						new ImmutableFloatPair(0.0f, -50.0f), 5.0f);
				ps.setPosition(new ImmutableFloatPair(aX * getTileWidth()
						+ getTileWidth() * 0.75f, y * getTileHeight()
						+ getTileHeight() * 0.75f));
				addEntity(ps);
			}
		}

		myMiddleLayer.setDestroyedToTileRow(aX);
		myForegroundLayer.setDestroyedToTileRow(aX);
	}

	public Collection<Entity> getEntities() {
		return mEntities;
	}

}
