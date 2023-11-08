package com.udacity.asteroidradar.main

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.AsteroidApplication
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.bindAsteroidStatusImage
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        val viewModelFactory = MainViewModelFactory((requireActivity().application as AsteroidApplication).repository)
        ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        val adapter = AsteroidAdapter(AsteroidListener { asteroidId ->
            viewModel.onAsteroidSelect(asteroidId)
        })

        binding.asteroidRecycler.adapter = adapter


        //observe the asteroid list from the viewModel
        viewModel.asteroids.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            Log.d("asteroids observer ", "The observer was called")
        })

        //observe for details navigation
        viewModel.navigate.observe(viewLifecycleOwner, Observer {
            if(it){
                viewModel.selectedAsteroid.value?.let{ asteroid: Asteroid ->
                        this.findNavController().navigate(MainFragmentDirections.actionShowDetail(asteroid))
                        viewModel.doneNavigating()
                }
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val today = LocalDate.now()
        val filter: (Asteroid) -> Boolean =
            when(item.itemId){
                R.id.show_today_menu -> {asteroid ->
                    LocalDate.parse(asteroid.closeApproachDate).isEqual(today)
                }
                R.id.show_week_menu -> { asteroid ->
                    LocalDate.parse(asteroid.closeApproachDate).isAfter(today) && LocalDate.parse(
                        asteroid.closeApproachDate
                    ).isBefore(today.plusDays(8))
                }
                R.id.show_past_menu -> {asteroid ->
                    LocalDate.parse(asteroid.closeApproachDate).isBefore(today)
                }
                else -> {asteroid -> true}
            }

        viewModel.updateFilter(filter = filter)
        return true
    }
}
