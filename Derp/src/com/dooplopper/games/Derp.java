package com.dooplopper.games;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.openal.Audio;

public class Derp extends BasicGame {
	
	protected static int width = 640;
	protected static int height = 480;
	private int stage = 1;
	private int stageCount = 3;
	private int view = 0;
	private int stageEnemies = 0;
	
	private static boolean fullScreen = true;
	private boolean up = false;
	private boolean down = false;
	private boolean left = false;
	private boolean right = false;
	private boolean drawing = false;
	private boolean transitioning = false;
	private boolean collides = false;
	private boolean useWASD = true;
	private boolean showStats = true;
	
	private Player pawn = new Player(new Rectangle(0, 0, width / 16, height / 16), 0, 0, 5);
	private Rectangle highlight = new Rectangle(0, 0, 0, 0);
	private Circle mouseCircle = new Circle(0, 0, 16);
	
	private Vector2f pawnHeading = new Vector2f();
	private Vector2f mouseLoc = new Vector2f();
	private Vector2f mouseLocStart = new Vector2f();
	
	ArrayList<Shape> objects = new ArrayList<Shape>();
	ArrayList<Color> colors = new ArrayList<Color>();
	ArrayList<Enemy> enemies = new ArrayList<Enemy>();
	ArrayList<Enemy> deadEnemies = new ArrayList<Enemy>();
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	ArrayList<Bullet> deadBullets = new ArrayList<Bullet>();
	ArrayList<PowerUp> powerUps = new ArrayList<PowerUp>();
	ArrayList<PowerUp> deadPowerUps = new ArrayList<PowerUp>();
	
	private Music backgroundMusic;
	private ArrayList<Sound> pews = new ArrayList<Sound>();
	
	private Random rand = new Random();
	
	private long lastUpdate;
	private long enemyUpdate;
	private long nextStage;
	private long transition;
	private long lastPew;
	
	/*private float lX;
	private float lY;
	private float rX;
	private float rY;
	
	private Controller controller;*/

	public Derp(String title) {
		super(title);
		
	}
	
	public double getAngle(float x1, float y1, float x2, float y2) {
		return Math.atan2(y2 - y1, x2 - x1);
		
	}
	
	public Vector2f getAngleVector(double angle, float radius) {
		Vector2f vec = new Vector2f();
		vec.set((float) (Math.cos(angle) * radius) * -1, (float) (Math.sin(angle) * radius) * -1);
		return vec;
		
	}
	
	public int getWidth() {
		return width;
		
	}
	
	public int getHeight() {
		return height;
		
	}
	
	public void updateCollision() {
		for(Bullet b: bullets) {
			if(b.getHealth() != 0) {
				for(Enemy e: enemies) {
					if(e.getHealth() != 0) {
						if(b.getShape().intersects(e.getShape())) {
							b.setVisible(false);
							b.setHealth(b.getHealth() - 1);
							e.setVisible(false);
							e.setHealth(e.getHealth() - 1);
							
						}
						
					}
					
				}
				
			}
			
		}
		
		for(Enemy e: enemies) {
			if(e.getHealth() > 0 && pawn.getHealth() > 0) {
				if(pawn.getShape().intersects(e.getShape())) {
					if(System.currentTimeMillis() - pawn.lastAttack > 1000) {
						pawn.setHealth(pawn.getHealth() - 1);
						e.setHealth(e.getHealth() - 1);
						pawn.lastAttack = System.currentTimeMillis();
						
					}
					
				}
				
			}
			
		}
		
		for(PowerUp p: powerUps) {
			if(pawn.getShape().intersects(p.getShape())) {
				p.setHealth(0);
				if(p.effect == 0) {
					if(!(pawn.getHealth() >= 5)) {
						pawn.setHealth(pawn.getHealth() + 1);
						
					}
					
				} /*else if(p.effect == 1) {
					if(!pawn.effects.containsKey(1)) {
						pawn.effects.put(1, System.currentTimeMillis());
						
					}
					
				}*/
				
			}
			
		}
		
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		if(view == 0) {
			g.setColor(Color.blue);
			g.drawString("Welcome to... Derp?", width / 2 - 100, height / 2 - 20);
			
		}else if(view == 1) {
			g.setColor(Color.yellow);
			if(showStats) {
				g.drawString("Up: " + up + " | Down: " + down + " | Left: " + left + " | Right: " + right + " | Health: " + pawn.getHealth() + " | Drawing: " + drawing + " | Stage: " + stage + " | Enemies: " + enemies.size() + " | Collision: " + collides + " | Pawn Heading: x: " + pawn.xVel + " -- y: " + pawn.yVel, 0, height - 20);
				
			}
			
			if(transitioning) {
				if(transition >= 1000) {
					if(stageCount != 0) {
						g.drawString("Next Stage: " + stageCount, width / 2 - 40, height / 2 - 10);
						stageCount -= 1;
						
					}
					
				}
				
			}
			
			g.setColor(Color.blue);
			g.draw(pawn.getShape());
			
			g.setColor(Color.red);
			
			if(drawing == true) {
				g.draw(highlight);
				
			}
			
			for(Shape s: objects) {
				g.draw(s);
				
			}
			
			g.setColor(colors.get(2));
			for(Enemy e: enemies) {
				g.draw(e.getShape());
				
			}
			
			g.setColor(Color.green);
			for(Bullet b: bullets) {
				g.draw(b.getShape());
				
			}
			
			for(PowerUp p: powerUps) {
				if(p.effect == 0) {
					g.setColor(Color.red);
					
				} else if(p.effect == 1) {
					g.setColor(Color.orange);
					
				} else if(p.effect == 2) {
					g.setColor(Color.cyan);
					
				}
				g.draw(p.getShape());
				
			}
			
			g.setColor(Color.green);
			g.draw(mouseCircle);
			
			g.setColor(Color.red);
			g.fillRect(width - 100, 0, 100 * (pawn.getHealth() / 5f), 20);
			
		} else if(view == 2) {
			g.setColor(Color.red);
			g.drawString("Game Over!", width / 2 - 100, height / 2 - 20);
			
		}
		
	}

	@Override
	public void init(GameContainer gc) throws SlickException {
		System.out.println(System.getProperty("user.dir"));
		backgroundMusic = new Music("/res/rerezzed.ogg");
		pews.add(new Sound("/res/pew1.ogg"));
		pews.add(new Sound("/res/pew2.ogg"));
		pews.add(new Sound("/res/pew3.ogg"));
		pews.add(new Sound("/res/pew4.ogg"));
		colors.add(new Color(0, 223, 38));
		colors.add(new Color(16, 6, 159));
		colors.add(new Color(Color.magenta));
		backgroundMusic.play(1, .75f);
		pawnHeading.set(0, 0);
		pawn.setHealth(3);
		lastUpdate = System.currentTimeMillis();
		enemyUpdate = System.currentTimeMillis();
		lastPew = System.currentTimeMillis();
		pawn.lastAttack = System.currentTimeMillis();
		
		/*try {
			Controllers.create();
			
		} catch (LWJGLException e) {
			e.printStackTrace();
			
		}*/
		
		//Controllers.poll();
		//controller = Controllers.getController(7);
		
	}

	@Override
	public void update(GameContainer gc, int delta) throws SlickException {
		Input in = gc.getInput();
		
		if(in.isKeyDown(Input.KEY_ESCAPE)) {
			gc.exit();
			
		}
		
		//lX = controller.getXAxisValue();
		
		if(view == 0) {
			if(in.isKeyPressed(Input.KEY_SPACE)) {
				view = 1;
				
			}
			
		} else if(view == 1) {
			if(in.isKeyPressed(Input.KEY_F3)) {
				showStats = !showStats;
				
			}
			
			if(in.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
				if(System.currentTimeMillis() - lastPew >= 100) {
					if(pawn.effects.containsKey(1)) {
						pews.get(1).play(1f, .5f);
						Vector2f bVec = new Vector2f();
						bVec.set(getAngleVector(getAngle(mouseLoc.getX(), mouseLoc.getY(), pawn.getShape().getCenterX(), pawn.getShape().getCenterY()), 2));
						Bullet b1 = new Bullet(new Circle(pawn.getShape().getCenterX(),  pawn.getShape().getCenterY(), 8), bVec.getX() + .5f, bVec.getY() + .5f, 1);
						Bullet b2 = new Bullet(new Circle(pawn.getShape().getCenterX(),  pawn.getShape().getCenterY(), 8), bVec.getX(), bVec.getY(), 1);
						Bullet b3 = new Bullet(new Circle(pawn.getShape().getCenterX(),  pawn.getShape().getCenterY(), 8), bVec.getX() - .5f, bVec.getY() - .5f, 1);
						bullets.add(b1);
						bullets.add(b2);
						bullets.add(b3);
						lastPew = System.currentTimeMillis();
						
					} else {
						pews.get(0).play(.5f, .5f);
						Vector2f bVec = new Vector2f();
						bVec.set(getAngleVector(getAngle(mouseLoc.getX(), mouseLoc.getY(), pawn.getShape().getCenterX(), pawn.getShape().getCenterY()), 2));
						Bullet b = new Bullet(new Circle(pawn.getShape().getCenterX(),  pawn.getShape().getCenterY(), 8), bVec.getX(), bVec.getY(), 1);
						bullets.add(b);
						lastPew = System.currentTimeMillis();
						
					}
					
				}
				
			}
			
			mouseLoc.set(in.getMouseX(), in.getMouseY());
			
			mouseCircle.setCenterX(mouseLoc.getX());
			mouseCircle.setCenterY(mouseLoc.getY());
			
			/*if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
				if(drawing == true) {
					objects.add(new Rectangle(highlight.getX(), highlight.getY(), highlight.getWidth(), highlight.getHeight()));
					drawing = false;
					
				} else {
					mouseLocStart.set(in.getMouseX(), in.getMouseY());
					drawing = true;
					
				}
				
			}*/
			
			if(in.isKeyDown(Input.KEY_W)) {
				up = true;
				
			} else {
				up = false;
				
			}
			
			if(in.isKeyDown(Input.KEY_S)) {
				down = true;
				
			} else {
				down = false;
				
			}
			
			if(in.isKeyDown(Input.KEY_A)) {
				left = true;
				
			} else {
				left = false;
				
			}
			
			if(in.isKeyDown(Input.KEY_D)) {
				right = true;
				
			} else {
				right = false;
				
			}
			
			if(useWASD) {
				if(up == true && down == true) {
					pawnHeading.set(pawnHeading.getX(), 0);
					
				} else if(up == true) {
					pawnHeading.set(pawnHeading.getX(), -1);
					
				} else if(down == true) {
					pawnHeading.set(pawnHeading.getX(), 1);
					
				} else {
					pawnHeading.set(pawnHeading.getX(), 0);
					
				}
				
				if(left == true && right == true) {
					pawnHeading.set(0, pawnHeading.getY());
					
				} else if(left == true) {
					pawnHeading.set(-1, pawnHeading.getY());
					
				} else if(right == true) {
					pawnHeading.set(1, pawnHeading.getY());
					
				} else {
					pawnHeading.set(0, pawnHeading.getY());
					
				}
				
			} else {
				pawnHeading.set(getAngleVector(getAngle(mouseLoc.getX(), mouseLoc.getY(), pawn.getShape().getX(), pawn.getShape().getY()), 1));
				
			}
			
			if(System.currentTimeMillis() - enemyUpdate > 1000) {
				if(transitioning) {
					if(System.currentTimeMillis() - nextStage > 3000) {
						stage += 1;
						transitioning = false;
						
					}
					
				} else {
					
					if(stageEnemies >= stage * 5) {
						enemies.clear();
						stageEnemies = 0;
						transitioning = true;
						nextStage = System.currentTimeMillis();
						transition = System.currentTimeMillis();
						
					} else {
						if(rand.nextInt(10) == 1) {
							powerUps.add(new PowerUp(new Circle(rand.nextInt(width), rand.nextInt(height), 16), 0, 0, 1, rand.nextInt(2)));
							
						} else {
							enemies.add(new Enemy(new Circle(rand.nextInt(width), rand.nextInt(height), 16), .5f, .5f, 2));
							stageEnemies += 1;
							
						}
						
					}
					
				}
				
				enemyUpdate = System.currentTimeMillis();
				
			}
			
			if(System.currentTimeMillis() - lastUpdate > 16) {
				if(pawn.getHealth() <= 0) {
					view = 2;
					
				} else {
					if(pawnHeading.getX() == 1 && pawnHeading.getY() == 1) {
						pawn.xVel = getAngleVector(-3 * Math.PI / 4, 1).getX();
						pawn.yVel = getAngleVector(-3 * Math.PI / 4, 1).getY();
						
					} else if(pawnHeading.getX() == -1 && pawnHeading.getY() == 1) {
						pawn.xVel = getAngleVector(-Math.PI / 4, 1).getX();
						pawn.yVel = getAngleVector(-Math.PI / 4, 1).getY();
						
					} else if(pawnHeading.getX() == -1 && pawnHeading.getY() == -1) {
						pawn.xVel = getAngleVector(Math.PI / 4, 1).getX();
						pawn.yVel = getAngleVector(Math.PI / 4, 1).getY();
						
					} else if(pawnHeading.getX() == 1 && pawnHeading.getY() == -1) {
						pawn.xVel = getAngleVector(3 * Math.PI / 4, 1).getX();
						pawn.yVel = getAngleVector(3 * Math.PI / 4, 1).getY();
						
					} else {
						pawn.xVel = pawnHeading.getX();
						pawn.yVel = pawnHeading.getY();
						
					}
					pawn.update();
					
				}
				
				updateCollision();
				
				for(Enemy e: enemies) {
					if(e.getHealth() == 0) {
						deadEnemies.add(e);
						
					} else {
						e.update();
						
					}
					
				}
				
				for(Enemy e: deadEnemies) {
					enemies.remove(e);
					
				}
				deadEnemies.clear();
				
				for(Bullet b: bullets) {
					if(b.getHealth() == 0) {
						deadBullets.add(b);
						
					} else {
						b.update();
						
					}
					
				}
				
				for(Bullet b: deadBullets) {
					bullets.remove(b);
					
				}
				deadBullets.clear();
				
				for(PowerUp p: powerUps) {
					if(p.getHealth() == 0) {
						deadPowerUps.add(p);
						
					} else {
						p.update();
						
					}
					
				}
				
				for(PowerUp p: deadPowerUps) {
					powerUps.remove(p);
					
				}
				deadPowerUps.clear();
				
				if(drawing == true) {
					highlight.setLocation(mouseLocStart);
					highlight.setWidth(mouseLoc.getX() - mouseLocStart.getX());
					highlight.setHeight(mouseLoc.getY() - mouseLocStart.getY());
					
				}
				
			}
			
		} else if(view == 2) {
			if(in.isKeyPressed(Input.KEY_SPACE)) {
				pawn.setHealth(3);
				view = 0;
				
			}
			
		}
		
		if(in.isKeyPressed(Input.KEY_Z)) {
			if(objects.size() != 0) {
				objects.remove(objects.size() - 1);
				
			}
			
		}
		
	}
	
	public static void main(String[] args) {
		try {
			AppGameContainer appgc = new AppGameContainer(new Derp(""));
			width = appgc.getScreenWidth();
			height = appgc.getScreenHeight();
			appgc.setDisplayMode(width, height, fullScreen);
			appgc.setShowFPS(true);
			appgc.start();
			
		} catch (SlickException e) {
			e.printStackTrace();
			
		}
		
	}

}
