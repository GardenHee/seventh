/*
 * see license.txt 
 */
package seventh.ai.basic.actions;

import java.util.List;

import seventh.ai.basic.Brain;
import seventh.game.Entity;
import seventh.game.PlayerEntity;
import seventh.map.PathFeeder;
import seventh.math.Vector2f;
import seventh.shared.TimeStep;

/**
 * Cover a friend
 * 
 * @author Tony
 *
 */
public class CoverEntityAction extends AdapterAction {
	
	private Entity followMe;
	private PathFeeder<?> feeder;
	private Vector2f previousPosition;
	
	private long lastVisibleTime;
	private final long timeSinceLastSeenExpireMSec;
	
	/**
	 * @param feeder
	 */
	public CoverEntityAction(Entity followMe) {
		this.followMe = followMe;
		this.previousPosition = new Vector2f();
		
		timeSinceLastSeenExpireMSec = 5_000;
	}
	
	/* (non-Javadoc)
	 * @see palisma.ai.Action#end(palisma.ai.Brain)
	 */
	@Override
	public void end(Brain brain) {		
		brain.getMotion().emptyPath();
	}
	
	/* (non-Javadoc)
	 * @see seventh.ai.basic.actions.AdapterAction#interrupt(seventh.ai.basic.Brain)
	 */
	@Override
	public void interrupt(Brain brain) {
		this.feeder = null;
	}

	/* (non-Javadoc)
	 * @see palisma.ai.Action#isFinished()
	 */
	@Override
	public boolean isFinished(Brain brain) {		
		return !this.followMe.isAlive() || this.lastVisibleTime > timeSinceLastSeenExpireMSec;
	}
	
	/* (non-Javadoc)
	 * @see palisma.ai.Action#update(palisma.ai.Brain, leola.live.TimeStep)
	 */
	@Override
	public void update(Brain brain, TimeStep timeStep) {
		
		List<PlayerEntity> entitiesInView = brain.getSensors().getSightSensor().getEntitiesInView();
		if(!entitiesInView.contains(this.followMe)) {		
			this.lastVisibleTime += timeStep.getDeltaTime();
		}
		else {
			this.lastVisibleTime = 0;
		}
		
		if(feeder == null || !feeder.onFirstNode()) {
			Vector2f newPosition = this.followMe.getPos();
			Vector2f start = brain.getEntityOwner().getPos();
			float distance = Vector2f.Vector2fDistanceSq(start, newPosition);
			
			
			if(distance > 1_000) {
				feeder = brain.getWorld().getGraph().findPath(start, newPosition);			
				brain.getMotion().setPathFeeder(feeder);	
			}			
			else {
				/* stop the agent */
				if(feeder != null) {
					feeder = null;
					brain.getMotion().setPathFeeder(feeder);
				}
			}
			
			previousPosition.set(newPosition);
		}		
	}

}
