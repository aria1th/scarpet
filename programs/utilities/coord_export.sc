// functions starting with __ means it is calculating function, which does not returns result for you
//Returns structures' coordinates without generation or loading, reliable than external amidst and very precise
//origin inputs should be specified with chunk coordinates, but outputs are block coordinates


__circle_biome(origin, dist, biomename, include_soul_sand) ->
(
	l(x,z) = origin;
	dist = abs(dist);
	l(xmin, xmax) = l(x-dist, x+dist+1);
	l(zmin, zmax) = l(z-dist, z+dist+1);
	c_for(ax=xmin, ax<xmax, ax+=1,
		c_for(az=zmin, az< zmax, az+=1,
			if( (ax - x)^2 + (az - z) ^2 <= dist ^2 && !( lower(biome(l(16*ax, 0, 16*az)))==lower(biomename) || (include_soul_sand && lower(biome(l(16*ax, 0, 16*az)))==lower('soul_sand_valley'))  ),return(0)
			)			
		)
	);
	return(1)
);

// If all biomes in range are biomename(mainly warped forest) or soul sand valley& include_soul_sand, return 1. Else, return 0.

__get_structure(xz) -> 
(
	l(x,z) = xz;
	return (keys(structure_eligibility(l(x,0,z))));
);

// check structure names without directly loading chunk.


__check_warped_fortress(xz, dist, include_soul_sand) ->
(
	l(x,z) = xz;
	l(alpha, omega)= (get((structure_eligibility(l(x,0,z),'fortress',512)), 'box'));
	mid = (alpha + omega)/2 ;
	l(cx, cy, cz) = mid / 16;
	l(cx, cy, cz) = l(floor(cx), floor(cy), floor(cz));
	return (__circle_biome(l(cx,cz), dist, 'warped_forest', include_soul_sand));
);

// Check coordinate whether it can has fortress, then size of it, and midpoint, and finally check its surrounding biome.

__end_ship(xyz) -> 
(
	nxyz = l();
	for ( xyz, 
		nxyz += abs(_)
	);
	l(a,b,c) = nxyz;
	absxyz = m(a,b,c);
	return (has(absxyz , 28)&&has( absxyz , 23)&&has( absxyz , 12))
);

// Check end city component is End ship component.

__find_purpur(xz) -> 
(
	l(x,z) = xz;
	return ((structure_eligibility(l(x,128, z),'end_city'))==1)
);

//Check if coordinate can have end_city


__has_ship(xz) ->
(
	return (
		if(__find_purpur(xz),
			l(x,z) = xz;
			ship = 0;
			ecpdata = get((structure_eligibility(l(x, 62, z), 'end_city', 256)),'pieces');
			ecplocs = l();
			first(ecpdata,
				 if( 
				__end_ship( (get(_,2) - get(_,3) ) ),  ship = 1
				)  
			);
		ship,0)
	)
);

// Merged function to check if coordinate can have end ship


__get_nebcr(selist)->
(
	listed = l();
	if(!selist, return(false),
		pieces = selist:'pieces';
		for(pieces, if(get(_,0) == 'nebcr',newlist = l();newlist += get(_,2);newlist += get(_,3);listed += newlist)
			);
		return (listed))
);

__distance(p1, p2)->
(
	(reduce(p1 - p2, _a+_^2,0))^0.5
);
__calc_distribution(bcrlist)->
(
	if(length(bcrlist)<5, return(10000));
	listed = l();
	for(bcrlist, l(a,b) = _; listed += (a+b)/2);
	i=l(0,0,0);for(listed,i=i+_);
	midpoint= i / length(listed);
	reduce(listed, _a+__distance(_, midpoint),0)/length(i)
);
//pieces: crossload, checks if fortress has 5 crossways


__distributed(coord)->(
l(a,b) = coord;
__calc_distribution(__get_nebcr(structure_eligibility(l(a,0,b), 'fortress', 256)))
);

//calculates crossroad distribution distance. normally < 33 is great place to place.

__hasmagma(coord)->(
l(a,b) = coord;
bpd = get(structure_eligibility(l(a,0,b), 'bastion_remnant', 512),'pieces');
for (bpd , if( _:3- _:2  == l(23, 10, 23), return(true)   )
 );
false
);
// for bastion remnants, finds if it has magmacube spawner.


__in(a,b,c)->(
l(aa, ab, ac) = a;
l(ba, bb, bc) = b;
l(ca, cb, cc) = c;
return (aa <= ba && ba <= ca && ab <= bb && bb <= cb && ac <= bc && bc <= cc)
);
// checks if one coord is between two coords



//functions for player below


amidst_ship(origin, dist) ->
(
	listed = l();
	l(ox, oz) = origin;
	for(rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;p=l(get(p,0),get(p,-1));
		if(__has_ship(p),
			listed+= p)
	);
	return(listed)	
);
// Returns end cities coordinate which has end ship, from origin within distance(chunk)
// usage: /script in coord_export run amidst_ship( l(0,0), 100) = from chunk origin 0,0, checks 100 chunk distance (+- 1600, +-1600)


amidst_ship_count (n,limit)->(
	add_up = l(1,0);
	add_down = l(-1,0);
	add_left = l(0,1);
	add_right = l(0,-1);
	checkspot = l(l(0,0));
	up = l(l(0,0));
	down = l(l(0,0));
	left = l(l(0,0));
	right = l(l(0,0));
	listed = l();
	i=0;
	for(rect(0,0,0,100,0,100), test:l(get(pos(_),0),get(pos(_),2)) = 0);
	while(length(listed)<n, limit,i+=1;
		ul = l();dl = l(); ll = l(); rl = l();tl = l();
		for(up, ul+= _+add_up);ul += l(i,-i);up=ul;
		for(down, dl+= _+add_down);dl += l(-i,i);down=dl;
		for(left, ll+= _+add_left);ll += l(i,i);left=ll;
		for(right, rl+= _+add_right);rl += l(-i,-i);right=rl;
		for(l(ul, dl, ll, rl), il = _;
			for(il, 
				if(abs(get(_,0))==i, np = l(get(_,0), -get(_,1)), np= l(-get(_,0), get(_,1)));

				p = _*16;delete(test, _); delete(test, np); np = np * 16;
				if(__has_ship(p),
					listed+=p);
				if(abs(get(np,0))==abs(get(np,1)), continue());
				if(__has_ship(np),
					listed+=np);
			));	
	);
	return(listed)
);

//It founds amounts of end ships from nearest. 
// /script in coord_export run amdist_ship_count(5, 1000) = finds 5 ships within 1000 distance

amidst_nether(origin, dist) ->
(
	Dict = m(l('ruined_portal',l()),l('fortress',l()),l('bastion_remnant',l()));
	l(ox, oz) = origin;
	for(rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;p=l(get(p,0),get(p,-1));
		coord = p;
		for(__get_structure(coord),
			if(has(Dict,lower(_)), 
				put( get(Dict, _), null,  coord, 'insert')
				
			);
		)
	);
	return(Dict)
);
// Returns nether structures'coordinates(ruined portal, fortress, bastion remnant), from origin within distance(chunk)
// usage: /script in coord_export run amidst_nether( l(0,0), 16)  = from chunk origin 0,0, checks 16 chunk distance 

amidst(origin, dist) ->
(
	Dict = m();
	l(ox, oz) = origin;
	for(rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;p=l(get(p,0),get(p,-1)); 
		coord = p;
		for(__get_structure(coord),
			if(has(Dict,lower(_)), 
				put( get(Dict, _), null,  coord, 'insert'),
				put( Dict, lower(_), l(coord))
			);
		)
	);
	return(Dict)
);
// Returns structures'coordinates, from origin within distance(chunk)
// usage /script in coord_export run amidst (l(0,0), 100) = from chunk origin 0,0, checks 100 chunk distance

amidst_structure(origin, dist, structure_name) ->
(
	Dict = l();
	l(ox, oz) = origin;
	for(rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;p=l(get(p,0),get(p,-1)); 
		coord = p;
		for(__get_structure(coord),
			if(lower(_)==lower(structure_name), 
				Dict+=p
			);
		)
	);
	return(Dict)
);
// Returns structures'coordinates, from origin within distance(chunk)
// usage /script in coord_export run amidst_structure (l(0,0), 100, structure_name) = from chunk origin 0,0, checks 100 chunk distance for 'structure_name', so maybe 'monument'



amidst_magmacube(origin, dist) ->
(
	Dict = l();
	l(ox, oz) = origin;
	for(rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;p=l(get(p,0),get(p,-1)); 
		coord = p;
		for(__get_structure(coord),
			if(lower(_)=='bastion_remnant', 
				c = __hasmagma(p);
				if( c, Dict+=p)
			);
		)
	);
	return(Dict)
);
// finds magma cube spawner. It's very fast!
//usage : /script in coord_export run amidst_magmacube(l(0,0),100)

amidst_warped_fortress(dist, finds, checkrange, include_soul_sand, density) ->
(
	add_up = l(1,0);
	add_down = l(-1,0);
	add_left = l(0,1);
	add_right = l(0,-1);
	checkspot = l(l(0,0));
	up = l(l(0,0));
	down = l(l(0,0));
	left = l(l(0,0));
	right = l(l(0,0));
	listed = l();
	i=0;
	while(length(listed)<finds, dist,i+=1;
		ul = l();dl = l(); ll = l(); rl = l();tl = l();
		for(up, ul+= _+add_up);ul += l(i,-i);up=ul;
		for(down, dl+= _+add_down);dl += l(-i,i);down=dl;
		for(left, ll+= _+add_left);ll += l(i,i);left=ll;
		for(right, rl+= _+add_right);rl += l(-i,-i);right=rl;
		for(l(ul, dl, ll, rl), il = _;
			for(il, 
				if(abs(get(_,0))==i, np = l(get(_,0), -get(_,1)), np= l(-get(_,0), get(_,1)));
				p = _*16;np = np * 16;
				for(__get_structure(p),
					if(lower(_)=='fortress'&& __distributed(p)<density&&__check_warped_fortress(p, checkrange, include_soul_sand), 
					listed+=p;found+=1;if(length(listed)>= finds, return (listed) )
					);
				);
				if(abs(get(np,0))==abs(get(np,1)), continue());
				for(__get_structure(np),
					if(lower(_)=='fortress'&& __distributed(np)<density&&__check_warped_fortress(np, checkrange, include_soul_sand), 
					listed+=np;found+=1;if(length(listed)>= finds, return (listed) )	
					);
				)
			)
		);	
	);
	return(listed)
);

// Returns Fortress surrounded with warped forest or soul sand valley, finds = fortresses to find, checkrange = surrounding range of biomes(8+ asserts maximum efficiency)
// coordinates input should be chunk positions, dist = chunk range(1 chunk = 16 block range), finds = number to find, checkrange = to vaild range(8 = 128 block)
// If you want to find fortress with warped forest biome only, surrounded by 6 chunk distances, in range of 1000 chunk, from origin (0,0), and find only one anywhere then
// usage : script in coord_export run amidst_warped_fortress ( 10000, 1, 8 ,1, 40) = from (0,0) check 1000 range find 1 surrounded by 6 chunk distance with only warped forest within 40 density. density is distribution of crossways
//, so set by 1000 if you don't care about it.


// if you set include_soul_sand 1 then it means you will allow soul sand valley & warped forest in checkrange.


amidst_gold_farm(origin, dist, finds, checkrange) ->
(
	listed = l();
	found = 0;
	l(ox, oz) = origin;
	for (rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;p=l(get(p,0),get(p,-1));
		if(__circle_biome(p/16, checkrange, 'nether_wastes', false), listed += p;found+=1;if(found > finds, return(listed)))
	);
	return (listed)
);

// Finds places just as 1.15 and before, where nether wastes only exists in checkrange. 
// usage: script in coord_export invoke amidst_gold_farm l(0,0) 1000 1 7 = Find from origin (0,0), for 1000 distances, find one and stop, assert it is surrounded by 7 chunk distances

amidst_fortressfarm(origin, dist) ->
(
	Dict = m();
	l(ox, oz) = origin;
	for(rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;coord=l(get(p,0),get(p,-1));
		se = structure_eligibility(p, 'fortress', 256);
		if(se, put(Dict, __calc_distribution(__get_nebcr(se)), coord))
	);
	print(min(keys(Dict)));
	print(Dict:min(keys(Dict)));
	return(Dict)
);

//for 1.15 or before, finds fortresses with 5 crossways, with closest distribution.
//usage : amidst_fortressfarm(l(0,0),1000)

